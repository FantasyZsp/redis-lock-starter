package com.sishu.redis.test;

import com.sishu.redis.RootTest;
import com.sishu.redis.lock.redisson.business.AnnotatedRepeatableStringListLock;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author ZSP
 */
public class AnnotatedRepeatableStringListLockTest extends RootTest {

  @Autowired
  private AnnotatedRepeatableStringListLock annotatedRepeatableStringListLock;


  @Test
  public void test() {
    annotatedRepeatableStringListLock.annotatedRepeatableTest("head", List.of("1", "222222222", "33333333333333"), "tail");
    System.out.println();
  }

}