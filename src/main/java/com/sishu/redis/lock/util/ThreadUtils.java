package com.sishu.redis.lock.util;

import java.util.concurrent.TimeUnit;

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

  public static void sleepSeconds(long timeout) {
    try {
      TimeUnit.SECONDS.sleep(timeout);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }


  public static void startAndJoin(Thread thread, long mills) {
    if (!thread.isAlive()) {
      thread.start();
    }
    try {
      thread.join(mills);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public static void startAndJoin(Thread thread) {
    if (!thread.isAlive()) {
      thread.start();
    }
    try {
      thread.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public static void start(Thread thread) {
    thread.start();
  }

  public static void join(Thread thread) {
    try {
      thread.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
