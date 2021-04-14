package com.sishu.redis.lock.support.exception;

import com.sishu.redis.lock.annotation.RedisLock;

import java.util.Set;

/**
 * @author ZSP
 */
public interface ExceptionSupplier {

  RuntimeException newException(RedisLock redisLock) throws Exception;

  default boolean support(ExceptionTag exceptionTag) {
    return supportedList().contains(exceptionTag.getClass());
  }

  default Set<Class<? extends ExceptionTag>> supportedList() {
    return Set.of(DefaultExceptionTag.class);
  }

}
