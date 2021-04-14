package com.sishu.redis.lock.support.exception;

import com.sishu.redis.lock.annotation.RedisLock;

import java.lang.reflect.Constructor;

/**
 * @author ZSP
 */
public class DefaultExceptionSupplier implements ExceptionSupplier {

  @Override
  public RuntimeException newException(RedisLock redisLock) throws Exception {
    Constructor<?> constructor = redisLock.exceptionClass().getConstructor(String.class);
    return (RuntimeException) constructor.newInstance(redisLock.exceptionMessage());
  }


}
