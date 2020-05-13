package com.sishu.redis.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
  @ConditionalOnMissingBean
  public Config redissonSingleServerConfig(ObjectMapper objectMapper) {
    Config config = new Config();
    config.setCodec(new JsonJacksonCodec(objectMapper));
    return config;
  }

  @Bean
  @DependsOn("redissonSingleServerConfig")
  @ConfigurationProperties(prefix = "redisson")
  @ConditionalOnMissingBean
  public SingleServerConfig singleServerConfig(Config redissonSingleServerConfig) {
    return redissonSingleServerConfig.useSingleServer();
  }


  @Bean
  @DependsOn("singleServerConfig")
  @ConditionalOnMissingBean
  public RedissonClient redissonClient(Config redissonSingleServerConfig) {
    log.info("redissonClient init. address:{}, database:{}", redissonSingleServerConfig.useSingleServer().getAddress(),
      redissonSingleServerConfig.useSingleServer().getDatabase());
    return Redisson.create(redissonSingleServerConfig);
  }
}
