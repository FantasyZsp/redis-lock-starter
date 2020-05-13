package com.sishu.redis.lock.redisson;

import com.sishu.redis.RootTest;
import com.sishu.redis.lock.util.ThreadUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBucket;
import org.redisson.api.RKeys;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/**
 * @author ZSP
 */
@Slf4j
public class RedissonLockTest extends RootTest {
  @Autowired(required = false)
  private RedissonClient redissonClient;


  private Runnable lockTask = () -> {
    final String lockName = "lockWithTime";
    RLock lock = redissonClient.getLock(lockName);
    try {
      ThreadUtils.join(100);
      lock.lock(100000, TimeUnit.MILLISECONDS);
      ThreadUtils.join(10000);
    } finally {
      lock.forceUnlock();
    }
  };

  @Before
  public void before() {
    log.info("redis database：{}", redissonClient.getConfig().useSingleServer().getDatabase());
  }


  @Test
  public void testLong() {
    RAtomicLong myLong = redissonClient.getAtomicLong("myLong");
    myLong.set(1000L);

    RAtomicLong myLong2 = redissonClient.getAtomicLong("myLong");
    long value = myLong2.get();
    log.info("value: {}", value);
    myLong.compareAndSet(1000L, 2000L);
  }

  @Test
  public void testKeys() {
    RKeys keys = redissonClient.getKeys();
    log.info("keys : {}", keys);
    log.info("keys.count() : {}", keys.count());

    for (String s : keys.getKeys()) {
      System.out.println(s);
    }
  }

  @Test
  public void lock() {
    final String lockName = "anyLock";
    RLock lock = redissonClient.getLock(lockName);
    // 可重入，value中计数
    lock.lock();
    lock.lock();
    tryLock(lockName);
  }

  @Test(expected = IllegalMonitorStateException.class)
  public void lockWithTime() {
    final String lockName = "lockWithTime";
    RLock lock = redissonClient.getLock(lockName);
    lock.lock(100, TimeUnit.MILLISECONDS);
    ThreadUtils.join(101);
    lock.unlock();
  }

  @Test
  public void lockWithTime2() {
    final String lockName = "lockWithTime";
    RLock lock = redissonClient.getLock(lockName);
    RBucket<Object> bucket = redissonClient.getBucket(lockName);
    log.info("getLock后lock前值情况: {}", bucket.get());

    try {
      // TODO 如何用缓存的方式拿到锁的值
      lock.lock(100, TimeUnit.SECONDS);
      RBucket<Object> bucket2 = redissonClient.getBucket(lockName);
      log.info("lock后值情况: {}", bucket2.get());

      ThreadUtils.join(101);
    } finally {
      lock.forceUnlock();
    }
  }

  private void tryLock(final String lockName) {
    Callable<Boolean> task = () -> {
      RLock anyLock = redissonClient.getLock(lockName);
      return anyLock.tryLock();
    };
    FutureTask<Boolean> futureTask = new FutureTask<>(task);
    new Thread(futureTask, "T-tryLock").start();

    try {
      log.info("获取情况: {}", futureTask.get());
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void concurrentLock() {
    new Thread(lockTask, "T-1").start();
    new Thread(lockTask, "T-2").start();
    ThreadUtils.join(500000);
  }

  @Test
  public void lockForever() {
    final String lockName = "lockForever";
    RLock lock = redissonClient.getLock(lockName);
    // -1，不释放的话会有线程一直续命
    lock.lock(-1, TimeUnit.MILLISECONDS);
    ThreadUtils.join(1000);
    lock.unlock();
  }

  @Test
  public void lockForever2() {
    final String lockName = "lockForever2";
    RLock lock = redissonClient.getLock(lockName);
    // 同 -1
    lock.lock();
    lock.unlock();
  }

}