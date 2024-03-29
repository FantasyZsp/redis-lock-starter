# redis-lock-starter

#### 介绍

自定义注解实现基于redis的分布式锁

#### 使用说明

1. 注解使用请查看@RedisLock相关JavaDoc。目前基于单机redis实现。

#### 更新日志

##### 1.1.2

1. 使用spel表达式集合映射的方式支持同时加N把锁，解决多资源加锁问题。
2. 修复几个问题
    - 锁route命名拼接重复问题
    - 加锁解锁日志
    - 屏蔽底层错误，强行包装为指定异常类型和信息

##### 1.1.3

1. 调整自动装配策略，允许引用方进行定制。

##### 1.1.3.1

1. route属性调整为prefix，消除歧义。

##### 1.1.3.2

1. 自动装配优化。防止redisClient需要过早初始化导致异常。

##### 1.2.0

1. 支持@Repeatable语义。
2. 修复有参数列表时常量key的错误

##### 1.3.0

目录重构

##### 1.4.0

1. 精简依赖
    - 去除apache common包
2. 升级依赖
    - springboot 2.6.9
    - redisson 3.18.0
3. 目录重构
4. 删除注解属性route

##### 1.4.1

1. 重构测试目录
2. 分离锁功能与redissonClient的自动装配逻辑
3. 修复空密码时的错误
4. 单测断言
5. 去除apache common包

##### 1.4.2-snapshot

1. 切面注入bean name为redissonClient4Lock的redissonClient

#### TODO
