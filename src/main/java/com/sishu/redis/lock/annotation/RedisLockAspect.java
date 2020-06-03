package com.sishu.redis.lock.annotation;

import com.sishu.redis.lock.util.SpelExpressionParserUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

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

  private static final String NAME_SPACE = "RL:";
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
    List<RLock> lockList = getLockList(route, lockKey);
    boolean lockSuccess = false;
    try {
      lockBatch(annotation, lockList);
      lockSuccess = true;
      log.debug("lock batch success");
      // do business or next aspect
      result = pjp.proceed();
    } catch (Throwable e) {

      if (lockSuccess) {
        log.debug("lock success but business error: {}", e.getMessage());
      } else {
        log.error("lock batch failed");
      }
      throw e;
    } finally {
      // mark 解决 重入情况下会直接释放锁而不是减重入次数。
      // 当加锁失败时，需要注意是否需要解锁
      if (lockSuccess) {
        unlockBatch(lockList);
      }
    }
    return result;
  }

  private void unlockBatch(List<RLock> lockList) {
    if (CollectionUtils.isEmpty(lockList)) {
      return;
    }
    // 逆序解锁
    lockList.sort(Comparator.comparing(RLock::getName).reversed());
    for (RLock rLock : lockList) {
      log.debug("解锁: {}", rLock.getName());
      rLock.unlock();
    }
  }


  private List<RLock> getLockList(String route, Object lockKey) {
    return getLockList(appendLockNameList(route, lockKey));
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

  /**
   * 当批量加锁失败时，需要释放已加的锁
   */
  private void lockBatch(RedisLock annotation, List<RLock> lockList) throws Exception {
    if (CollectionUtils.isEmpty(lockList)) {
      return;
    }
    List<RLock> successList = new ArrayList<>(lockList.size());
    for (RLock rLock : lockList) {
      try {
        lock(annotation, rLock.getName(), rLock);
        successList.add(rLock);
      } catch (Exception e) {
        log.debug("release locks when lock ex： {}", successList);
        successList.forEach(Lock::unlock);
        throw e;
      }
    }
  }


  private void lock(RedisLock redisLock, String lockName, RLock lock) throws Exception {
    long waitTime = redisLock.waitTime();
    boolean waitForeverWhenHeldByOtherThread = waitTime < 0;
    long leaseTime = redisLock.leaseTime();
    TimeUnit timeUnit = redisLock.timeUnit();
    Class<?> exceptionClass = redisLock.exceptionClass();
    String exceptionMessage = redisLock.exceptionMessage();

    log.debug("attempt to lock: {}", lockName);


    // 预期内的上锁结果，代指无论上锁成功与否都没有抛出代码本身异常。预期外的结果如解锁错误，寻址可用服务错误，系统error等。
    boolean lockResultExpected = true;
    Throwable throwable = null;

    if (waitForeverWhenHeldByOtherThread) {

      try {
        lock.lock(leaseTime, timeUnit);
        log.debug("lock success: {}", lockName);
      } catch (Throwable ex) {
        lockResultExpected = false;
        throwable = ex;
        log.error("lock failed unexpected, lockName: {}, error reason:{}", lockName, ex.getMessage());
      }

    } else {

      boolean getLock = true;
      try {
        getLock = lock.tryLock(waitTime, leaseTime, timeUnit);
      } catch (Throwable ex) {
        lockResultExpected = false;
        throwable = ex;
        log.error("try lock failed unexpected, lockName: {}, error reason:{}", lockName, ex.getMessage());
      }

      if (!getLock) {
        Constructor<?> constructor = exceptionClass.getConstructor(String.class);
        RuntimeException exception = (RuntimeException) constructor.newInstance(exceptionMessage);
        log.error("try lock failed: {}", lockName);
        throw exception;
      } else {
        log.debug("try lock success: {}", lockName);
      }
    }

    // 包装并抛出预期外错误
    if (!lockResultExpected) {
      log.debug("unexpected ex when redis lock working, lockName: {}", lockName, throwable);
      Constructor<?> constructor = exceptionClass.getConstructor(String.class);
      throw (RuntimeException) constructor.newInstance(exceptionMessage);
    }


  }

  private String appendLockName(String route, Object lockKey) {
    Assert.notNull(lockKey, "must not be null");
    if (lockKey instanceof String) {
      Assert.hasText((String) lockKey, "must not be empty");
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

      // 去重。不去重可能会导致重复加锁，多了一次网络io
      // 去空。不加锁空的对象
      lockKey = ((Collection<?>) lockKey).stream().distinct()
        .filter(Objects::nonNull)
        .collect(Collectors.toCollection(ArrayList::new));

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
