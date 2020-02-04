package com.sishu.redis.lock.annotation;

import com.sishu.redis.lock.util.SpelExpressionParserUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 拦截RedisLock注解方法切面
 * <p>
 * 排他可重入
 *
 * @author ZSP
 */
@Aspect
@Slf4j
public class RedisLockAspect implements Ordered {

  private static final String NAME_SPACE = "REDIS_LOCK:";
  private static final String SEPARATOR = ":";

  private RedissonClient redissonClient;


  @Pointcut("@annotation(com.sishu.redis.lock.annotation.RedisLock)")
  public void redisLockPointCut() {
  }

  @Around("redisLockPointCut()")
  public Object redisLockAround(ProceedingJoinPoint pjp) throws Throwable {
    Method targetMethod = this.getTargetMethod(pjp);
    RedisLock annotation = targetMethod.getAnnotation(RedisLock.class);

    String route = annotation.route();
    String keySpel = annotation.key();
    String lockKey = SpelExpressionParserUtils.generateKeyByEl(keySpel, pjp);

    Object result;
    String lockName = appendLockName(route, lockKey);
    RLock lock = redissonClient.getLock(lockName);

    boolean lockSuccess = false;
    try {
      lock(annotation, lockName, lock);
      lockSuccess = true;
      result = pjp.proceed();
    } catch (Throwable e) {
      if (lockSuccess) {
        log.error("business error");
      } else {
        log.error("lock failed");
      }
      throw e;
    } finally {
      // mark 解决 重入情况下会直接释放锁而不是减重入次数。
      // 当加锁失败时，需要注意是否需要解锁
      if (lockSuccess) {
        log.info("解锁: {}", lock.getName());
        lock.unlock();
      }
    }
    return result;
  }

  private void lock(RedisLock redisLock, String lockName, RLock lock) throws InterruptedException, NoSuchMethodException, InstantiationException, IllegalAccessException, java.lang.reflect.InvocationTargetException {
    long waitTime = redisLock.waitTime();
    boolean waitForeverWhenHeldByOtherThread = waitTime < 0;
    long leaseTime = redisLock.leaseTime();
    TimeUnit timeUnit = redisLock.timeUnit();
    Class<?> exceptionClass = redisLock.exceptionClass();
    String exceptionMessage = redisLock.exceptionMessage();

    log.info("尝试加锁: {}", lockName);
    if (waitForeverWhenHeldByOtherThread) {
      lock.lock(leaseTime, timeUnit);
    } else {
      boolean getLock = lock.tryLock(waitTime, leaseTime, timeUnit);
      if (!getLock) {
        Constructor<?> constructor = exceptionClass.getConstructor(String.class);
        RuntimeException exception = (RuntimeException) constructor.newInstance(exceptionMessage);
        log.error("获取锁失败: {}", lockName);
        throw exception;
      }
    }
  }

  private String appendLockName(String route, String lockKey) {
    Assert.hasText(lockKey, "must not be empty");
    if (StringUtils.isNotBlank(route)) {
      route = route + SEPARATOR;
    }
    return NAME_SPACE + route + lockKey;
  }


  /**
   * 获取目标方法
   */
  private Method getTargetMethod(ProceedingJoinPoint pjp) throws NoSuchMethodException {
    Signature signature = pjp.getSignature();
    MethodSignature methodSignature = (MethodSignature) signature;
    Method agentMethod = methodSignature.getMethod();
    return pjp.getTarget().getClass().getMethod(agentMethod.getName(), agentMethod.getParameterTypes());
  }

  public void setRedissonClient(RedissonClient redissonClient) {
    this.redissonClient = redissonClient;
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }

  public void init() {
    log.info("RedisLockAspect init...");
  }
}
