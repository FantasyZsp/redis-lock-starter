package com.sishu.redis;

import com.sishu.redis.lock.redission.string.RedisLockAnnotationStringListLock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * @author ZSP
 */
@SpringBootTest(classes = AppTest.class)
@RunWith(SpringRunner.class)
public class RootTest {

  @Autowired
  private RedisLockAnnotationStringListLock redisLockAnnotationStringListLock;


  @Test
  public void test()  {
    redisLockAnnotationStringListLock.multiKey(List.of("1", "222222222", "33333333333333"));
    System.out.println();
  }
}
