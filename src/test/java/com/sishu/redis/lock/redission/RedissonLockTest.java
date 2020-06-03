package com.sishu.redis.lock.redission;

import com.sishu.redis.RootTest;
import com.sishu.redis.lock.util.ThreadUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBucket;
import org.redisson.api.RFuture;
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


  @Test
  public void tryLock() {
    final String lockName = "tryLock";
    RLock lock = redissonClient.getLock(lockName);
    // 可重入，value中计数
    try {
      boolean lockResult = lock.tryLock(1, -1, TimeUnit.SECONDS);
      if (lockResult) {
        log.info("上锁成功");
//        throw new RuntimeException("业务错误");
      } else {
        log.info("上锁失败");
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (RuntimeException e) {
      log.info("error: {}", e.getCause().toString());

      throw e;
    } finally {
      lock.unlock();
    }
//    if (lock.isLocked()) {
//    }


    Runnable runnable = () -> {
      try {
        RLock lock1 = redissonClient.getLock(lockName);
        System.out.println(lock1);

        boolean lockResult = lock1.tryLock(10, -1, TimeUnit.SECONDS);
        if (lockResult) {
          log.info("thread 上锁成功");
        } else {
          log.info("thread 上锁失败");
        }
      } catch (InterruptedException e) {
        log.info("error");
        e.printStackTrace();
      }
    };

    Thread thread = new Thread(runnable);
    Thread thread2 = new Thread(runnable);
    thread.start();
    thread2.start();

    ThreadUtils.sleepSeconds(30);
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

  @Test(expected = IllegalMonitorStateException.class)
  public void lockWithReleaseTime() {
    final String lockName = "lockWithReleaseTime";
    RLock lock = redissonClient.getLock(lockName);
    // 5s后释放
    lock.lock(5, TimeUnit.SECONDS);
    ThreadUtils.sleepSeconds(7);
    lock.unlock();
  }

  @Test
  public void lockWithReleaseTime2() {
    final String lockName = "lockWithReleaseTime";
    RLock lock = redissonClient.getLock(lockName);
    // 5s后释放
    lock.lock(5, TimeUnit.SECONDS);
    ThreadUtils.sleepSeconds(7);

    if (lock.isHeldByCurrentThread()) {
      log.info("解锁");
      lock.unlock();
    } else {
      log.info("没有解锁");
    }
  }

  @Test(expected = IllegalMonitorStateException.class)
  public void lockWithReleaseTime3() {
    final String lockName = "lockWithReleaseTime3";
    RLock lock = redissonClient.getLock(lockName);
    // 5s后释放
    lock.lock(3, TimeUnit.SECONDS);
    ThreadUtils.join(2900);

    if (lock.isHeldByCurrentThread()) {
      ThreadUtils.join(200);
      lock.forceUnlock();

      log.info("解锁");
      lock.unlock();
    } else {
      log.info("没有解锁");
    }
  }

  @Test(expected = IllegalMonitorStateException.class)
  public void lockWithReleaseTime4() {
    final String lockName = "lockWithReleaseTime4";
    RLock lock = redissonClient.getLock(lockName);
    // 5s后释放
    lock.lock(2, TimeUnit.SECONDS);
    ThreadUtils.join(1900);

    if (lock.isHeldByCurrentThread()) {
      ThreadUtils.join(200);
      RFuture<Void> voidRFuture = lock.unlockAsync();
      voidRFuture.join();

      log.info("解锁");
      lock.unlock();
    } else {
      log.info("没有解锁");
    }
  }

  @Test(expected = IllegalMonitorStateException.class)
  public void lockWithReleaseTime5() {
    final String lockName = "lockWithReleaseTime5";
    RLock lock = redissonClient.getLock(lockName);
    // 5s后释放
    lock.lock(2, TimeUnit.SECONDS);
    ThreadUtils.join(1900);

    if (lock.isHeldByCurrentThread()) {
      ThreadUtils.join(200);
      RFuture<Void> voidRFuture = lock.unlockAsync();
      RFuture<Void> voidRFuture2 = lock.unlockAsync();
      RFuture<Void> voidRFuture3 = lock.unlockAsync();
      RFuture<Void> voidRFuture4 = lock.unlockAsync();
      RFuture<Void> voidRFuture5 = lock.unlockAsync();
      RFuture<Void> voidRFuture6 = lock.unlockAsync();

      System.out.println(voidRFuture.isDone());
      log.info("解锁");
      lock.unlock();
    } else {
      log.info("没有解锁");
    }
  }

}