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
   * 功能模块或者资源类别，用于细化锁的粒度，拼接在key前面
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

  /**
   * 自动释放锁的时间。
   * 请合理预估业务需要时间，防止锁提前被释放。
   * -1表明线程在占有锁期间不会因为执行时间过长导致锁释放。
   * 正数代表在指定时间后将自动释放锁。
   */
  long leaseTime() default -1;

  TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

  Class<? extends RuntimeException> exceptionClass() default RuntimeException.class;

  String exceptionMessage() default "服务器繁忙，请稍后重试！";


}
