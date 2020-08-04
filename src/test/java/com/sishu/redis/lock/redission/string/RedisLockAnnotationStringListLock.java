package com.sishu.redis.lock.redission.string;

import com.sishu.redis.RootTest;
import com.sishu.redis.lock.annotation.RedisLock;
import com.sishu.redis.lock.redission.GirlDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 配合测试注解
 *
 * @author ZSP
 */
@Slf4j
@Service
public class RedisLockAnnotationStringListLock extends RootTest {


  // age -> girlDto
  private static final Map<Integer, GirlDTO> DATABASES = new HashMap<>();

  static {
    DATABASES.put(1, new GirlDTO(1, "1", 1));
    DATABASES.put(2, new GirlDTO(2, "2", 2));
    DATABASES.put(3, new GirlDTO(3, "3", 3));
  }


  @RedisLock(key = "#girls", exceptionMessage = "String test", waitTime = 0, exceptionClass = NullPointerException.class)
  public void multiKey(List<String> girls) {
    log.info("multiKey invoke...");


  }

//  @RedisLock(route = "redis-lock", key = "T(java.lang.String).valueOf(#girl1.id).concat(':').concat(#girl2.id)", waitTime = 0)
//  public String keyConcat(GirlDTO girl1, GirlDTO girl2) {
//    ThreadUtils.join(100);
//    return "success";
//  }


}
