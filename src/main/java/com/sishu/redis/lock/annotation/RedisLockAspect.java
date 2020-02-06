package com.sishu.redis.lock.annotation;

import com.sishu.redis.lock.util.SpelExpressionParserUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    Object lockKey = SpelExpressionParserUtils.generateKeyByEl(keySpel, pjp);


    Object result;

    if (lockKey instanceof Collection) {
      return pjp.proceed();
    }
    List<String> lockNameList = appendLockNameList(route, lockKey);
    List<RLock> lockList = getLockList(lockNameList);

    boolean lockSuccess = false;
    try {
      lockBatch(annotation, lockNameList, lockList);
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
//        log.info("解锁: {}", lock.getName());
//        lock.unlock();
      }
    }
    return result;
  }

  private void lockBatch(RedisLock annotation, List<String> lockNameList, List<RLock> lockList) {

  }

  private List<RLock> getLockList(List<String> lockNameList) {
    Assert.notNull(lockNameList, "must not be null");
    List<RLock> rLockList = new ArrayList<>(lockNameList.size());
    // 顺序加锁防死锁
    Collections.sort(lockNameList);
    for (String lockName : lockNameList) {
      rLockList.add(redissonClient.getLock(lockName));
    }
    return rLockList;
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

  private String appendLockName(String route, Object lockKey) {
    Assert.notNull(lockKey, "must not be null");
    if (lockKey instanceof String) {
      Assert.hasText((String) lockKey, "must not be empty");
    }
    if (StringUtils.isNotBlank(route)) {
      route = route + SEPARATOR;
    }
    return NAME_SPACE + route + lockKey;
  }

  private List<String> appendLockNameList(String route, Object lockKey) {
    Assert.notNull(lockKey, "must not be null");
    if (lockKey instanceof String) {
      Assert.hasText((String) lockKey, "must not be empty");
    }

    if (StringUtils.isNotBlank(route)) {
      route = route + SEPARATOR;
    }

    List<String> lockNameList = new ArrayList<>(objectLength(lockKey));
    // 暂不考虑map
    if (lockKey instanceof Collection) {
      for (Object key : (Collection<?>) lockKey) {
        lockNameList.add(appendLockName(route, key));
      }
    } else {
      lockNameList.add(appendLockName(route, lockKey));
    }
    return lockNameList;
  }

  public static int objectLength(Object object) {
    Objects.requireNonNull(object);
    if (object instanceof Collection) {
      return ((Collection<?>) object).size();
    }

    if (object instanceof Map<?, ?>) {
      return ((Map<?, ?>) object).size();
    }

    if (object.getClass().isArray()) {
      return ArrayUtils.getLength(object);
    }
    return 1;
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
