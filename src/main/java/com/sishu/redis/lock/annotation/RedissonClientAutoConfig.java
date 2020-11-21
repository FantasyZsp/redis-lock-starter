package com.sishu.redis.lock.annotation;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
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
@Configuration
@ConditionalOnClass(RedissonClient.class)
public class RedissonClientAutoConfig {

  @Configuration
  @ConditionalOnProperty(prefix = "redisson", name = "mode", havingValue = "single")
  @Slf4j
  static class SingleServerClientConfig {
    @Bean
    @ConditionalOnMissingBean(Config.class)
    public Config redissonSingleServerConfig() {
      Config config = new Config();
      config.setCodec(new JsonJacksonCodec());
      return config;
    }

    @Bean
    @DependsOn("redissonSingleServerConfig")
    @ConfigurationProperties(prefix = "redisson")
    @ConditionalOnMissingBean(SingleServerConfig.class)
    public SingleServerConfig singleServerConfig(Config redissonSingleServerConfig) {
      return redissonSingleServerConfig.useSingleServer();
    }

    @Bean
    @DependsOn("singleServerConfig")
    @ConditionalOnMissingBean(RedissonClient.class)
    public RedissonClient redissonClient(Config redissonSingleServerConfig) {
      log.info("single redissonClient init. address:{}, database:{}", redissonSingleServerConfig.useSingleServer().getAddress(),
        redissonSingleServerConfig.useSingleServer().getDatabase());
      return Redisson.create(redissonSingleServerConfig);
    }
  }


}
