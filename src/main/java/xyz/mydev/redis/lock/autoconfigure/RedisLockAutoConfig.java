package xyz.mydev.redis.lock.autoconfigure;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import xyz.mydev.redis.lock.annotation.RedisLock;
import xyz.mydev.redis.lock.aop.RepeatableRedisLockAspect;

/**
 * {@link RedisLock} 切面配置
 * 依赖 RedissonClient
 *
 * @author ZSP
 */
@AutoConfiguration(after = RedissonClientAutoConfig.class)
@ConditionalOnProperty(prefix = "redis-lock", name = "enable", havingValue = "true")
@Slf4j
public class RedisLockAutoConfig {

    @Value("${redis-lock.global-prefix:RL:}")
    private String globalPrefix;

    @Value("${redis-lock.showDeadLockWarning:false}")
    private boolean showDeadLockWarning;

    @Bean(initMethod = "init")
    public RepeatableRedisLockAspect redisLockAspect(@Qualifier("redissonClient4Lock") RedissonClient redissonClient) {
        RepeatableRedisLockAspect redisLockAspect = new RepeatableRedisLockAspect();
        redisLockAspect.setRedissonClient(redissonClient);
        redisLockAspect.setGlobalPrefix(globalPrefix == null ? "RL:" : globalPrefix);
        redisLockAspect.setShowDeadLockWarning(showDeadLockWarning);
        log.info("enable redis lock with global prefix: [{}]", globalPrefix);
        return redisLockAspect;
    }
}
