package com.sishu.redis.lock.annotation;

import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link RedisLock} 切面配置
 * 依赖 RedissonClient
 *
 * @author ZSP
 */
@Configuration
@ConditionalOnProperty(prefix = "redis-lock", name = "enable", havingValue = "true")
@AutoConfigureAfter(RedissonClientAutoConfig.class)
@ConditionalOnBean(RedissonClient.class)
public class RedisLockAnnotationSupportAutoConfig {

  @Bean(initMethod = "init")
  public RedisLockAspect redisLockAspect(RedissonClient redissonClient) {
    RedisLockAspect redisLockAspect = new RedisLockAspect();
    redisLockAspect.setRedissonClient(redissonClient);
    return redisLockAspect;
  }
}
