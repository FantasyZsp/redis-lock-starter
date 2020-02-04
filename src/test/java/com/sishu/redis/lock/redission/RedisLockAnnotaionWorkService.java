package com.sishu.redis.lock.redission;

import com.sishu.redis.lock.annotation.RedisLock;
import com.sishu.redis.lock.util.ThreadUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 配合测试注解
 *
 * @author ZSP
 */
@Slf4j
@Service
public class RedisLockAnnotaionWorkService {
  // age -> girlDto
  private static final Map<Integer, GirlDTO> DATABASES = new HashMap<>();

  static {
    DATABASES.put(1, new GirlDTO(1, "1", 1));
    DATABASES.put(2, new GirlDTO(2, "2", 2));
    DATABASES.put(3, new GirlDTO(3, "3", 3));
  }

  @RedisLock(route = "redis-lock", key = "#uniqueName", waitTime = 2000)
  public String lock(String uniqueName) {
    return uniqueName + " 插入成功！";
  }

  @RedisLock(route = "redis-lock", key = "#girlDTO.id", waitTime = 2000)
  public String lockWithDto(GirlDTO girlDTO) {
    return girlDTO + " 插入成功！";
  }

  @RedisLock(key = "#girl.age", exceptionMessage = "已经存在同年龄的记录", waitTime = 0)
  public void insertWithUniqueAge(GirlDTO girl) {
    GirlDTO girlList = DATABASES.get(girl.getAge());
    if (ObjectUtils.allNotNull(girlList)) {
      throw new RuntimeException("已经存在同年龄的记录");
    }
    ThreadUtils.join(1000);
    DATABASES.put(girl.getAge(), girl);
  }

}
