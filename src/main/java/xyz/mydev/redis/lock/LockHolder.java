package xyz.mydev.redis.lock;

import java.lang.annotation.Annotation;
import java.util.concurrent.locks.Lock;

/**
 * 对锁信息的封装
 *
 * @author ZSP
 */
public interface LockHolder {

    /**
     * 获取锁的引用
     *
     * @return Lock
     */
    Lock getLock();

    /**
     * 获取锁对应的注解信息，注解内部给出了锁相关的配置信息
     * 注意，可能注解本身附带的信息带有占位符，不能直接使用
     *
     * @return 注解
     */
    Annotation getAnnotation();

}
