package com.sishu.redis.lock.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 单机分布式锁
 * 事务提交后释放锁
 *
 * @author ZSP
 */
@Target(ElementType.METHOD)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisLock {

  /**
   * 功能模块，用于细化锁的粒度，拼接在key前面
   * route:key
   */
  String route() default "";

  /**
   * 键主体，粒度参考mysql innodb行锁。当粒度过大时影响并发。
   * 如tenantId_orgId_业务主键。
   */
  String key();

  /**
   * 尝试获取锁的等待时间
   * 小于0时一直阻塞等待，大于等于零时当超过指定时间会进入获取锁失败流程。
   */
  long waitTime() default -1;

  long leaseTime() default -1;

  TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

  Class<? extends RuntimeException> exceptionClass() default RuntimeException.class;

  String exceptionMessage() default "服务器繁忙，请稍后重试！";


}