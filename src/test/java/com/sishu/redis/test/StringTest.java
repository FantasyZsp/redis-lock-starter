package com.sishu.redis.test;

import com.sishu.redis.RootTest;
import com.sishu.redis.lock.redisson.GirlDTO;
import com.sishu.redis.lock.redisson.business.AnnotatedStringListLock;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * spEL各种写法搭配@RedisLock注解的加锁效果测试
 *
 * @author ZSP
 */
public class StringTest extends RootTest {

  @Autowired
  private AnnotatedStringListLock annotatedStringListLock;


  @Test
  public void test() {
    annotatedStringListLock.multiKey("head", List.of("1", "222222222", "33333333333333"), "tail");
    System.out.println();
  }

  @Test
  public void testFunction() {
    annotatedStringListLock.function(new GirlDTO(2, "cupSize", 1));
    System.out.println();
  }

  @Test
  public void collectionFieldKey() {
    AnnotatedStringListLock.ClassRoom classRoom = new AnnotatedStringListLock.ClassRoom();
    annotatedStringListLock.collectionFieldKey(classRoom);
    classRoom.setGirlIds(List.of("1", "2", "3"));
    annotatedStringListLock.collectionFieldKey(classRoom);
    System.out.println();
  }


  @Test
  public void testMultiConcatKey() {
    annotatedStringListLock.multiConcatKey("head", List.of("1", "222222222", "33333333333333"), "tail");
    System.out.println();
  }

  @Test
  public void testConstantKey() {
    annotatedStringListLock.constantKey("test");
    System.out.println();
  }

  @Test
  public void testLongKey() {
    annotatedStringListLock.longKey(11L);
    System.out.println();
  }
}
