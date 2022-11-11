package xyz.mydev.redis.lock.autoconfigure;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xyz.mydev.redis.lock.annotation.RedisLock;
import xyz.mydev.redis.lock.aop.RepeatableRedisLockAspect;

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
@Slf4j
public class RedisLockAutoConfig {

    @Value("${redis-lock.global-prefix:RL:}")
    private String globalPrefix;

    @Bean(initMethod = "init")
    public RepeatableRedisLockAspect redisLockAspect(RedissonClient redissonClient) {
        RepeatableRedisLockAspect redisLockAspect = new RepeatableRedisLockAspect();
        redisLockAspect.setRedissonClient(redissonClient);
        redisLockAspect.setGlobalPrefix(globalPrefix == null ? "RL:" : globalPrefix);
        log.info("enable redis lock with global prefix: [{}]", globalPrefix);
        return redisLockAspect;
    }
}
