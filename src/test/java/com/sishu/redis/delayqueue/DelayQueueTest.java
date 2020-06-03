package com.sishu.redis.delayqueue;

import com.sishu.redis.RootTest;
import com.sishu.redis.lock.util.ThreadUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Delayed;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ZSP
 */
@Slf4j
public class DelayQueueTest extends RootTest {
  @Autowired(required = false)
  private RedissonClient redissonClient;

  @Before
  public void before() {
    log.info("redis database：{}", redissonClient.getConfig().useSingleServer().getDatabase());
  }

  @After
  public void after() {
    redissonClient.shutdown();
  }

  @Test
  public void test() {


    int max = Integer.MAX_VALUE;
    Thread producer = new Producer(redissonClient, max, 2, 10);
    Thread producer2 = new Producer(redissonClient, max, 5, 20);
    ThreadUtils.start(producer);
    ThreadUtils.start(producer2);

    Thread consumer = new Consumer(redissonClient);
    Thread consumer2 = new Consumer(redissonClient);
    ThreadUtils.start(consumer);
    ThreadUtils.startAndJoin(consumer2);

  }

  /**
   * 没有去重
   */
  @Test
  public void testSameKey() {
    RBlockingQueue<Order> blockingFairQueue = redissonClient.getBlockingQueue("same_key");
    RDelayedQueue<Order> delayedQueue = redissonClient.getDelayedQueue(blockingFairQueue);
    Order order = Order.ofSeconds(1000);
    delayedQueue.offer(order, order.getDelay(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
    delayedQueue.offer(order, order.getDelay(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
    log.info("放入延时队列: {} {}", order.getId(), order.getInvalidTime());
    ThreadUtils.sleepSeconds(10000);
  }

  @Test
  public void testProducer() {
    Thread producer = new Producer(redissonClient, 10, 10, 60);
    Thread producer2 = new Producer(redissonClient, 10, 20, 180);
    ThreadUtils.start(producer);
    ThreadUtils.startAndJoin(producer2);
    ThreadUtils.join(producer);
  }

  @Test
  public void testConsume() {
    Consumer consumer = new Consumer(redissonClient);

    ThreadUtils.startAndJoin(consumer);
  }

  @Test
  public void testConsumeEx() {
    log.info("testConsumeEx start...");
    RBlockingQueue<Order> blockingFairQueue = redissonClient.getBlockingQueue("delay_queue");
    // 这里必须获取一下延时队列，否则blockingFairQueue.take()会一直阻塞
    redissonClient.getDelayedQueue(blockingFairQueue);
    Order order;
    try {
      order = blockingFairQueue.take();
      // 异常后，redis中被取出的这条数据会丢失
      log.info("获取到order: {} 后异常", order.getId());
      throw new RuntimeException();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }


  @Test
  public void testPeek() {
    log.info("testConsumeEx start...");
    RBlockingQueue<Order> blockingFairQueue = redissonClient.getBlockingQueue("delay_queue");
    // 这里必须获取一下延时队列，否则blockingFairQueue.take()会一直阻塞
    redissonClient.getDelayedQueue(blockingFairQueue);
    Order order = null;
    try {

      while (order == null) {
        order = blockingFairQueue.peek();
        if (order == null) {
          log.info("没有获取到...");
          ThreadUtils.join(100);
        }
      }

      log.info("获取到order: {}", order.getId());

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  public void getDelayedTime() {
    System.out.println(Order.ofSeconds(10).getDelay(TimeUnit.NANOSECONDS));
  }


}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class Order implements Delayed {
  private transient static AtomicInteger counter = new AtomicInteger();

  private String id;
  private String name;
  // 订单失效时间
  @NotNull
  private LocalDateTime invalidTime;

  @Override
  public long getDelay(TimeUnit unit) {
    long delay = unit.convert(Duration.between(LocalDateTime.now(), invalidTime));
    return delay > 0 ? delay : 0;
  }

  @Override
  public int compareTo(Delayed o) {
    return 0;
  }

  public static Order ofSeconds(long seconds) {
    return Order.builder()
      .id("id" + counter.incrementAndGet())
      .name("name" + counter.get())
      .invalidTime(LocalDateTime.now().plusSeconds(seconds))
      .build();
  }


}

@Slf4j
class Consumer extends Thread {
  private transient static AtomicInteger counter = new AtomicInteger();
  private RedissonClient redissonClient;

  public Consumer(RedissonClient redissonClient) {
    super("consumer-" + counter.getAndIncrement());
    this.redissonClient = redissonClient;
  }

  @Override
  public void run() {
    RBlockingQueue<Order> blockingFairQueue = redissonClient.getBlockingQueue("delay_queue");
    RDelayedQueue<Order> delayedQueue = redissonClient.getDelayedQueue(blockingFairQueue);
//    ThreadUtils.join(10_000);
    log.info("开始获取.......");

    while (true) {
      Order order;
      try {
        order = blockingFairQueue.take();
        log.info("订单[{}]失效时间: {}: ", order.getId(), order.getInvalidTime());
      } catch (InterruptedException ignore) {
      } catch (Exception e) {
        delayedQueue.destroy();
        throw new RuntimeException(e);
      }
    }
  }
}

@Slf4j
class Producer extends Thread {
  private transient static AtomicInteger counter = new AtomicInteger();
  private final RedissonClient redissonClient;
  private final int max;
  private final int minSecond;
  private final int maxSecond;

  public Producer(RedissonClient redissonClient, int max, int minSecond, int maxSecond) {
    super("producer-" + counter.getAndIncrement());
    this.redissonClient = redissonClient;
    this.max = max;
    this.minSecond = minSecond;
    this.maxSecond = maxSecond;
  }

  @Override
  public void run() {

    log.info("开始生产.......");
    RBlockingQueue<Order> blockingFairQueue = redissonClient.getBlockingQueue("delay_queue");

    RDelayedQueue<Order> delayedQueue = redissonClient.getDelayedQueue(blockingFairQueue);
    for (int i = 0; i < max; i++) {
      ThreadUtils.sleepSeconds(ThreadLocalRandom.current().nextInt(2, 10));
      Order order = Order.ofSeconds(ThreadLocalRandom.current().nextInt(minSecond, maxSecond));
      delayedQueue.offer(order, order.getDelay(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
      log.info("放入延时队列: {} {}", order.getId(), order.getInvalidTime());
    }
  }
}


