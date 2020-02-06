# redis-lock

#### 介绍
redis-client应用demo
1. 自定义注解实现基于redis的分布式锁

#### 使用说明

1. 注解使用请查看@RedisLock相关JavaDoc。目前基于单机redis实现。


#### 更新日志

1. 使用spel表达式集合映射的方式支持同时加N把锁，解决多资源加锁问题。

#### TODO