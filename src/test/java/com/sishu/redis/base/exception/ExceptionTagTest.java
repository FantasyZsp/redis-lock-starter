package com.sishu.redis.base.exception;

import com.sishu.redis.RootTest;
import com.sishu.redis.lock.redisson.business.AnnotatedStringListLock;
import com.sishu.redis.lock.util.ThreadUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author ZSP
 */
public class ExceptionTagTest extends RootTest {
  @Autowired
  private AnnotatedStringListLock annotatedStringListLock;


  @Test
  public void testExceptionType() {
    Runnable runnable = () -> annotatedStringListLock.base("testExceptionType");
    ThreadUtils.start(new Thread(runnable));
    ThreadUtils.start(new Thread(runnable));
    runnable.run();
    System.out.println();
  }



}
