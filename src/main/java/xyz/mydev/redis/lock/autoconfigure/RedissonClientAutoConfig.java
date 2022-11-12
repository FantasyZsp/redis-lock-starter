package xyz.mydev.redis.lock.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * 默认单机版
 * redisson.mode可选的值：
 * sentinel
 * masterSlave
 * single
 * cluster
 * replicated
 *
 * @author ZSP
 * @see org.redisson.config.Config
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(RedissonClient.class)
@ConditionalOnProperty(prefix = "redis-lock", name = "enable-client", havingValue = "true", matchIfMissing = true)
public class RedissonClientAutoConfig {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "redis-lock", name = "mode", havingValue = "single")
    @ConditionalOnExpression("${redis-lock.enable-client:true}")
    @Slf4j
    static class SingleServerClientConfig {
        @Bean("redissonSingleServerConfig")
        @ConditionalOnMissingBean(Config.class)
        public Config redissonSingleServerConfig(ObjectMapper objectMapper) {
            Config config = new Config();
            config.setCodec(new JsonJacksonCodec(objectMapper));
            return config;
        }

        @Bean("singleServerConfig")
        @DependsOn("redissonSingleServerConfig")
        @ConfigurationProperties(prefix = "redis-lock.redisson.single")
        @ConditionalOnMissingBean(SingleServerConfig.class)
        public SingleServerConfig singleServerConfig(Config redissonSingleServerConfig) {
            return redissonSingleServerConfig.useSingleServer();
        }

        @Bean("redissonClient4Lock")
        @DependsOn("singleServerConfig")
        @ConditionalOnMissingBean(name = "redissonClient4Lock")
        public RedissonClient redissonClient4Lock(Config redissonSingleServerConfig) {
            log.info("single redissonClient init. address:{}, database:{}", redissonSingleServerConfig.useSingleServer().getAddress(),
                redissonSingleServerConfig.useSingleServer().getDatabase());
            return Redisson.create(redissonSingleServerConfig);
        }
    }


}
