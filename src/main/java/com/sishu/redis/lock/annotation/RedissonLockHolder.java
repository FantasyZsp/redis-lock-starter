package com.sishu.redis.lock.annotation;

import org.redisson.RedissonLock;
import org.redisson.api.RLock;

/**
 * 对锁信息的封装
 *
 * @author ZSP
 */
public interface RedissonLockHolder extends LockHolder {

  /**
   * 获取锁的引用
   *
   * @return RLock
   */
  @Override
  RLock getLock();

  /**
   * 获取锁对应的注解信息，注解内部给出了锁相关的配置信息
   * 注意，可能注解本身附带的信息带有占位符，不能直接使用
   *
   * @return 注解
   */
  @Override
  RedisLock getAnnotation();

  /**
   * 是否使用同步解锁模式。
   * 同步解锁时需要关注解锁时的异常。如锁释放后或不存在时去执行解锁动作，{@link RedissonLock#unlockAsync(long)}将触发IllegalMonitorStateException异常。
   *
   * @return true if syncReleaseMode
   */
  default boolean useSyncReleaseMode() {
    return getAnnotation().leaseTime() == -1;
  }

  /**
   * 返回锁的完整名称
   *
   * @return String
   * @see RLock#getName()
   */
  default String getLockName() {
    return getLock().getName();
  }

}
