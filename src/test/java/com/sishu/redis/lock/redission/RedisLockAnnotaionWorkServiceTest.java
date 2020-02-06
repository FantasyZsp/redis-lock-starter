package com.sishu.redis.lock.redission;

import com.sishu.redis.RootTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ZSP
 */
public class RedisLockAnnotaionWorkServiceTest extends RootTest {
  @Autowired
  private RedisLockAnnotaionWorkService redisLockAnnotaionWorkService;
  private static final Map<Integer, GirlDTO> TEMP_DATABASES = new HashMap<>();

  static {
    TEMP_DATABASES.put(1, new GirlDTO(1, "1", 1));
    TEMP_DATABASES.put(2, new GirlDTO(2, "2", 2));
    TEMP_DATABASES.put(3, new GirlDTO(3, "3", 3));
  }

  @Test
  public void testMultiKey() {
    TEMP_DATABASES.put(4, new GirlDTO(4, "3", null));
    redisLockAnnotaionWorkService.multiKey(List.copyOf(TEMP_DATABASES.values()));
  }

  @Test
  public void testMultiKeyWithEmptyList() {
    redisLockAnnotaionWorkService.multiKey(new ArrayList<>());
  }

  @Test
  public void testMultiKeyWithNullList() {
    redisLockAnnotaionWorkService.multiKey(null);
  }

}