package xyz.mydev.redis.lock.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ZSP
 */
public class LogUtils {
  private static final Logger logger = LoggerFactory.getLogger(LogUtils.class);

  public static void print(String msg, Object... args) {
    logger.info(msg, args);
  }

  public static void print(Logger logger, String msg, Object... args) {
    logger.info(msg, args);
  }
}
