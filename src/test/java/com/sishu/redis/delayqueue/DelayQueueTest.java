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
//    redissonClient.shutdown();
  }

  @Test
  public void test() {


    Thread producer = new Producer(redissonClient);
    Thread producer2 = new Producer(redissonClient);
    ThreadUtils.start(producer);
    ThreadUtils.start(producer2);

    Thread consumer = new Consumer(redissonClient);
    Thread consumer2 = new Consumer(redissonClient);
    ThreadUtils.start(consumer);
    ThreadUtils.startAndJoin(consumer2);


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
  // 发货时间
  private LocalDateTime deliveryTime;
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
      .deliveryTime(LocalDateTime.now())
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
//    ThreadUtils.join(10_000);
    log.info("开始获取.......");

    while (true) {
      Order order;
      try {
        order = blockingFairQueue.take();
        log.info("订单[{}]失效时间: {}: ", order.getId(), order.getInvalidTime());
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}

@Slf4j
class Producer extends Thread {
  private transient static AtomicInteger counter = new AtomicInteger();
  private RedissonClient redissonClient;

  public Producer(RedissonClient redissonClient) {
    super("producer-" + counter.getAndIncrement());
    this.redissonClient = redissonClient;
  }

  @Override
  public void run() {

    log.info("开始生产.......");
    RBlockingQueue<Order> blockingFairQueue = redissonClient.getBlockingQueue("delay_queue");

    RDelayedQueue<Order> delayedQueue = redissonClient.getDelayedQueue(blockingFairQueue);
    for (; ; ) {
      ThreadUtils.sleepSeconds(ThreadLocalRandom.current().nextInt(2, 10));
      Order order = Order.ofSeconds(ThreadLocalRandom.current().nextInt(5, 20));
      delayedQueue.offer(order, order.getDelay(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
      log.info("放入延时队列: {} {}", order.getId(), order.getInvalidTime());
    }
  }
}


