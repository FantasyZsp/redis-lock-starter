package com.sishu.redis.lock.util;

/**
 * @author ZSP
 */
public class ThreadUtils {
  public static void join(long mills) {
    try {
      Thread.currentThread().join(mills);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public static void startAndJoin(Thread thread, long mills) {
    thread.start();
    try {
      thread.join(mills);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public static void start(Thread thread) {
    thread.start();
  }
}
