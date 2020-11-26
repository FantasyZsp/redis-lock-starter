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
import org.springframework.core.annotation.AnnotationUtils;
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
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * process {@link RedisLocks} and {@link RedisLock}
 *
 * @author ZSP
 */
@Aspect
@Slf4j
public class RepeatableRedisLockAspect implements Ordered {

  private static final String NAME_SPACE = "RL:";
  private static final String SEPARATOR = ":";

  private RedissonClient redissonClient;


  @Pointcut("@annotation(com.sishu.redis.lock.annotation.RedisLock)||@annotation(com.sishu.redis.lock.annotation.RedisLocks)")
  public void redisLocksPointCut() {
  }

  @Around("redisLocksPointCut()")
  public Object redisLocksAround(ProceedingJoinPoint pjp) throws Throwable {
    Method targetMethod = this.getTargetMethod(pjp);
    // linkedHashSet
    Set<RedisLock> annotations = AnnotationUtils.getDeclaredRepeatableAnnotations(targetMethod, RedisLock.class);

    Objects.requireNonNull(annotations);

    List<RedissonLockHolder> lockListSortedByGroup = getSortedLockListByGroup(annotations, pjp);

    Object result;
    boolean lockSuccess = false;
    try {
      lockBatch(lockListSortedByGroup);
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
      // Unlock when lock success. Doing by this way can avoid to unlock again when lock ex, which unlock already at that time.
      if (lockSuccess) {
        unlockBatch(lockListSortedByGroup);
      }
    }
    return result;
  }

  /**
   * @return lock holders that sorted by {@link RedisLock#order()}, using annotation declaring sort when unset.
   */
  private List<RedissonLockHolder> getSortedLockListByGroup(Set<RedisLock> annotations, ProceedingJoinPoint pjp) {
    List<RedissonLockHolder> sortedLockListByGroup = new ArrayList<>();

    ArrayList<RedisLock> sortedRedisLockAnnotation = new ArrayList<>(annotations);
    // sort by order that user setting
    sortedRedisLockAnnotation.sort(Comparator.comparing(RedisLock::order));

    for (RedisLock annotation : sortedRedisLockAnnotation) {
      String prefix = annotation.prefix();
      String keySpel = annotation.key();
      Object lockKey = SpelExpressionParserUtils.generateKeyByEl(keySpel, pjp);
      List<RLock> lockListSortedByName = getLockListSortedByName(prefix, lockKey);

      List<RedissonLockHolder> list = new ArrayList<>();
      for (RLock lock : lockListSortedByName) {
        RedissonLockHolderImpl redissonLockHolderImpl = new RedissonLockHolderImpl(lock, annotation);
        list.add(redissonLockHolderImpl);
      }
      sortedLockListByGroup.addAll(list);
    }

    return sortedLockListByGroup;
  }

  private void unlockBatch(List<RedissonLockHolder> lockList) {
    if (CollectionUtils.isEmpty(lockList)) {
      return;
    }

    // unlock revered with unsorted calculate
    int size = lockList.size();
    for (int i = size - 1; i >= 0; i--) {
      RedissonLockHolder lockHolder = lockList.get(i);
      log.debug("unlock: {}, unlock mode: {}", lockHolder.getLockName(), lockHolder.useSyncReleaseMode() ? "sync" : "async");
      if (lockHolder.useSyncReleaseMode()) {
        lockHolder.getLock().unlock();
      } else {
        // no care ex when unlock
        lockHolder.getLock().unlockAsync();
      }
    }

  }


  private List<RLock> getLockListSortedByName(String prefix, Object lockKey) {
    return getLockListSortedByName(appendLockNameList(prefix, lockKey));
  }

  private List<RLock> getLockListSortedByName(List<String> lockNameList) {
    Assert.notNull(lockNameList, "must not be null");
    List<RLock> rLockList = new ArrayList<>(lockNameList.size());
    // sort name to avoid deadlock
    Collections.sort(lockNameList);
    for (String lockName : lockNameList) {
      rLockList.add(redissonClient.getLock(lockName));
    }
    return rLockList;
  }

  private void lockBatch(List<RedissonLockHolder> lockList) throws Exception {
    if (CollectionUtils.isEmpty(lockList)) {
      return;
    }

    if (log.isWarnEnabled() && lockList.size() > 1) {
      RedissonLockHolder secondLockHolder = lockList.get(1);
      boolean waitForeverSomeTime = secondLockHolder.getAnnotation().waitTime() < 0;
      if (waitForeverSomeTime) {
        log.warn("WARNING!!!The second lock [{}] is not set waitTime when use lock-composed mode, maybe deadlock ", secondLockHolder.getLockName());
      }
    }

    List<RLock> successList = new ArrayList<>(lockList.size());
    for (RedissonLockHolder lockHolder : lockList) {
      try {
        lock(lockHolder);
        successList.add(lockHolder.getLock());
      } catch (Exception e) {
        log.debug("release locks when lock ex： {}", successList);
        if (lockHolder.useSyncReleaseMode()) {
          successList.forEach(RLock::unlock);
        } else {
          // no care ex when unlock
          successList.forEach(RLock::unlockAsync);
        }
        throw e;
      }
    }
  }


  private void lock(RedissonLockHolder lockHolder) throws Exception {

    RedisLock redisLock = lockHolder.getAnnotation();

    long waitTime = redisLock.waitTime();
    boolean waitForeverWhenHeldByOtherThread = waitTime < 0;

    long leaseTime = redisLock.leaseTime();
    TimeUnit timeUnit = redisLock.timeUnit();
    Class<?> exceptionClass = redisLock.exceptionClass();
    String exceptionMessage = redisLock.exceptionMessage();

    RLock lock = lockHolder.getLock();
    String lockName = lock.getName();

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
      boolean lockSuccess = true;
      try {
        lockSuccess = lock.tryLock(waitTime, leaseTime, timeUnit);
      } catch (Throwable ex) {
        lockResultExpected = false;
        throwable = ex;
        log.error("try lock failed unexpected, lockName: {}, error reason:{}", lockName, ex.getMessage());
      }

      if (!lockSuccess) {
        Constructor<?> constructor = exceptionClass.getConstructor(String.class);
        RuntimeException exception = (RuntimeException) constructor.newInstance(exceptionMessage);
        log.error("try lock failed: {}", lockName);
        throw exception;
      } else {
        log.debug("try lock success: {}", lockName);
      }
    }

    // throw an unexpected ex
    if (!lockResultExpected) {
      log.debug("unexpected ex when redis lock working, lockName: {}", lockName, throwable);
      Constructor<?> constructor = exceptionClass.getConstructor(String.class);
      throw (RuntimeException) constructor.newInstance(exceptionMessage);
    }


  }

  private String appendLockName(String prefix, Object lockKey) {
    Assert.notNull(lockKey, "must not be null");
    if (lockKey instanceof String) {
      Assert.hasText((String) lockKey, "must not be empty");
    }

    return NAME_SPACE + prefix + lockKey;
  }

  private List<String> appendLockNameList(String prefix, Object lockKey) {
    Assert.notNull(lockKey, "must not be null");
    if (lockKey instanceof String) {
      Assert.hasText((String) lockKey, "must not be empty");
    }

    if (StringUtils.isNotBlank(prefix)) {
      prefix = prefix + SEPARATOR;
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
        lockNameList.add(appendLockName(prefix, key));
      }
    } else {
      lockNameList.add(appendLockName(prefix, lockKey));
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
    return Ordered.HIGHEST_PRECEDENCE + 100;
  }

  public void init() {
    log.info("RepeatableRedisLockAspect init...");
  }
}
