package xyz.mydev.redis.lock.annotation;

import java.lang.annotation.*;

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
public @interface RedisLocks {

    RedisLock[] value();


}
