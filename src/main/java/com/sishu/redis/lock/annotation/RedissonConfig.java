package com.sishu.redis.lock.annotation;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;

/**
 * @author ZSP
 */
@Slf4j
@ConditionalOnProperty(prefix = "redisson", name = "address")
public class RedissonConfig {

  @Bean
  public Config redissonSingleServerConfig() {
    Config config = new Config();
    config.setCodec(new JsonJacksonCodec());
    return config;
  }

  @Bean
  @DependsOn("redissonSingleServerConfig")
  @ConfigurationProperties(prefix = "redisson")
  public SingleServerConfig singleServerConfig(Config redissionSingleServerConfig) {
    return redissionSingleServerConfig.useSingleServer();
  }


  @Bean
  @DependsOn("singleServerConfig")
  public RedissonClient redissonClient(Config redissonSingleServerConfig) {
    log.info("redissonClient init...");
    return Redisson.create(redissonSingleServerConfig);
  }
}
