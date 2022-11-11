package xyz.mydev.redis.lock.impl;

import lombok.Getter;
import org.redisson.api.RLock;
import xyz.mydev.redis.lock.RedissonLockHolder;
import xyz.mydev.redis.lock.annotation.RedisLock;

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
