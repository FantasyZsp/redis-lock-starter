package com.sishu.redis.test.base;

import com.sishu.redis.RootTest;
import org.junit.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.locks.Lock;

/**
 * @author ZSP
 */
public class RedissonApiTest extends RootTest {
  @Autowired
  private RedissonClient redissonClient;

  @Test
  public void testTryLock() {
    Lock testTryLock = redissonClient.getLock("testTryLock");
    boolean b = testTryLock.tryLock();
    testTryLock.unlock();
    System.out.println(b);

    testTryLock = redissonClient.getLock("testTryLock");
    b = testTryLock.tryLock();
    testTryLock.unlock();

    System.out.println(b);
  }

}
