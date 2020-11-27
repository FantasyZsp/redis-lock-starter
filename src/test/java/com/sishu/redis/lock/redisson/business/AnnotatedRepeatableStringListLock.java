package com.sishu.redis.lock.redisson.business;

import com.sishu.redis.lock.annotation.RedisLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 配合测试注解
 *
 * @author ZSP
 */
@Slf4j
@Service
public class AnnotatedRepeatableStringListLock {
  //  @RedisLock(key = "#girls", exceptionMessage = "String test", waitTime = 0, exceptionClass = NullPointerException.class)
  @RedisLock(key = "'common-module'", exceptionMessage = "common-module lock failed", waitTime = -1)
  @RedisLock(key = "#girls", exceptionMessage = "common-module lock failed", waitTime = -1)
  public void annotatedRepeatableTest(String head, List<String> girls, String tail) {
    log.info("annotatedRepeatableTest invoke...");
  }
}
