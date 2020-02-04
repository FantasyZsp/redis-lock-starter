package com.sishu.redis.lock.annotation;

import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * @author ZSP
 */
@ConditionalOnProperty(prefix = "redis-lock", name = "enable", havingValue = "true")
@ConditionalOnBean(name = "redissonClient")
public class RedissonLockSupportConfig {

  @Bean(initMethod = "init")
  public RedisLockAspect redisLockAspect(RedissonClient redissonClient) {
    RedisLockAspect redisLockAspect = new RedisLockAspect();
    redisLockAspect.setRedissonClient(redissonClient);
    return redisLockAspect;
  }
}
