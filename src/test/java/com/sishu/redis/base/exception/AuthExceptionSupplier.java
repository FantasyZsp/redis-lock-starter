package com.sishu.redis.base.exception;

import com.sishu.redis.lock.annotation.RedisLock;
import com.sishu.redis.lock.support.exception.ExceptionSupplier;
import com.sishu.redis.lock.support.exception.ExceptionTag;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.util.Set;

/**
 * @author ZSP
 */
@Component
public class AuthExceptionSupplier implements ExceptionSupplier {

  static final String redisLockDefaultExMsg = "服务器繁忙，请稍后重试！";

  @Override
  public RuntimeException newException(RedisLock redisLock) throws Exception {

    String exceptionMessage = redisLock.exceptionMessage();
    AuthError authError = AuthError.valueOf(redisLock.exceptionTagName());
    if (redisLockDefaultExMsg.equals(exceptionMessage)) {
      // 按需处理默认消息
      exceptionMessage = authError.getInfo();
    }

    Constructor<?> constructor = redisLock.exceptionClass().getConstructor(redisLock.exceptionTag(), String.class);
    return (RuntimeException) constructor.newInstance(authError, exceptionMessage);
  }

  @Override
  public Set<Class<? extends ExceptionTag>> supportedList() {
    return Set.of(AuthError.class);
  }
}
