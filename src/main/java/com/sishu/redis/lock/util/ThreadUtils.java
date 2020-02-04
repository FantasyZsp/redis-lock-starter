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
}
