package com.sishu.redis.lock.redission.string;

import com.sishu.redis.RootTest;
import com.sishu.redis.lock.redission.GirlDTO;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author ZSP
 */
public class StringTest extends RootTest {

  @Autowired
  private RedisLockAnnotationStringListLock redisLockAnnotationStringListLock;


  @Test
  public void test() {
    redisLockAnnotationStringListLock.multiKey("head", List.of("1", "222222222", "33333333333333"), "tail");
    System.out.println();
  }

  @Test
  public void testFunction() {
    redisLockAnnotationStringListLock.function(new GirlDTO(2, "cupSize", 1));
    System.out.println();
  }

  @Test
  public void testMultiConcatKey() {
    redisLockAnnotationStringListLock.multiConcatKey("head", List.of("1", "222222222", "33333333333333"), "tail");
    System.out.println();
  }
}
