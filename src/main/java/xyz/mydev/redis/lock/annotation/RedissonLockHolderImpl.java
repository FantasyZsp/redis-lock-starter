package xyz.mydev.redis.lock.annotation;

import lombok.Getter;
import org.redisson.api.RLock;

/**
 * @author ZSP
 */
@Getter
public final class RedissonLockHolderImpl implements RedissonLockHolder {
  private final RLock lock;
  private final RedisLock annotation;

  public RedissonLockHolderImpl(RLock lock, RedisLock annotation) {
    this.lock = lock;
    this.annotation = annotation;
  }
}
