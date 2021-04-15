package com.sishu.redis.lock.support.exception;

import com.sishu.redis.lock.annotation.RedisLock;

import java.lang.reflect.Constructor;

/**
 * @author ZSP
 */
public final class DefaultExceptionSupplier implements ExceptionSupplier {

  public static DefaultExceptionSupplier INSTANT = new DefaultExceptionSupplier();

  @Override
  public RuntimeException newException(RedisLock redisLock) throws Exception {
    Constructor<?> constructor = redisLock.exceptionClass().getConstructor(String.class);
    return (RuntimeException) constructor.newInstance(redisLock.exceptionMessage());
  }


}
