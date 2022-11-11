package xyz.mydev.redis.lock.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 单机分布式锁
 * 基于AOP， 对于同个方法的注解，总会优先于事务注解开始，在事务提交后释放锁
 * 如果外层已经开始事务，则无法使锁免于事务隔离的影响，使用方需要注意
 * <p>
 * 注意：
 * <p>当注解组合使用时，必须注意多个业务间对加锁的顺序处理；为避免死锁，建议为每个加锁的方法注明尝试加锁的等待时间{@link RedisLock#waitTime()}为正数，防止死锁事故。</p>
 *
 * @author ZSP
 */
@Target(ElementType.METHOD)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(RedisLocks.class)
public @interface RedisLock {


    /**
     * key前缀，用于区分key命名空间，拼接在key前面
     * prefix:key
     */
    String prefix() default "";

    /**
     * 键主体，粒度参考mysql innodb行锁。当粒度过大时影响并发。
     * 如tenantId_orgId_业务主键。
     */
    String key();

    /**
     * 尝试获取锁的等待时间
     * 小于0时一直阻塞等待，大于等于零时当超过指定时间会进入获取锁失败流程。
     * 单位 {@link RedisLock#timeUnit()}
     */
    long waitTime() default -1;

    /**
     * 自动释放锁的时间。
     * 请合理预估业务需要时间，防止锁提前被释放。
     * -1表明线程在占有锁期间不会因为执行时间过长导致锁释放。
     * 正数代表在指定时间后将自动释放锁。
     * 单位 {@link RedisLock#timeUnit()}
     */
    long leaseTime() default -1;

    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

    Class<? extends RuntimeException> exceptionClass() default RuntimeException.class;

    String exceptionMessage() default "服务器繁忙，请稍后重试！";

    /**
     * 注解间排序顺序。默认都是0，即默认将会按照声明的顺序依次处理注解内部key。
     */
    int order() default 0;
}
