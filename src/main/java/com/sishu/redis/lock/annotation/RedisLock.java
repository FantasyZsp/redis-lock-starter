package com.sishu.redis.lock.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 单机分布式锁
 * 基于AOP， 对于同个方法的注解，总会优先于事务注解开始，在事务提交后释放锁
 * 如果外层已经开始事务，则无法使锁免于事务隔离的影响，使用方需要注意
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
   * 属性命名，容易引起歧义，推荐使用 prefix
   */
  @AliasFor("prefix")
  @Deprecated
  String route() default "";

  /**
   * key前缀，用于区分key命名空间，拼接在key前面
   * prefix:key
   * 替代原有route
   */
  @AliasFor("route")
  String prefix() default "";

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
