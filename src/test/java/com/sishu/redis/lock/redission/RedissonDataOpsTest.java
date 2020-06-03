package com.sishu.redis.lock.redission;

import com.sishu.redis.RootTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBucket;
import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

import static com.sishu.redis.lock.util.LogUtils.print;

/**
 * @author ZSP
 */
@Slf4j
public class RedissonDataOpsTest extends RootTest {
  @Autowired
  private RedissonClient redissonClient;

  @Before
  public void before() {
    log.info("redis database：{}", redissonClient.getConfig().useSingleServer().getDatabase());
  }


  @Test
  public void testLong() {
    RAtomicLong myLong = redissonClient.getAtomicLong("myLong");
    myLong.set(1000L);

    RAtomicLong myLong2 = redissonClient.getAtomicLong("myLong");
    long value = myLong2.get();
    log.info("value: {}", value);
    myLong.compareAndSet(1000L, 2000L);
  }

  @Test
  public void testKeys() {
    RKeys keys = redissonClient.getKeys();
    log.info("keys : {}", keys);
    log.info("keys.count() : {}", keys.count());

    for (String s : keys.getKeys()) {
      System.out.println(s);
    }
  }

  @Test
  public void testSetValue() {
    String key = "testSetValue";
    RBucket<String> myString = redissonClient.getBucket(key);
    print("是否存在：{}", myString.isExists());
    print("myString 值：{}", myString.get());

    myString.delete();
    print("删除后是否存在：{}", myString.isExists());
    print("myString 值：{}", myString.get());

    myString.set("init");
    // set nx px   false
    Assert.assertFalse(myString.trySet("init2", 1, TimeUnit.MINUTES));

    myString.delete();
    Assert.assertTrue(myString.trySet("init2", 1, TimeUnit.MINUTES));
    print("trySet后是否存在：{}", myString.isExists());
    print("myString 值：{}", myString.get());
  }

  @Test
  public void testStringOps() {
    RBucket<String> myString = redissonClient.getBucket("testStr");
    print("myString：{}", myString.isExists());
  }

  @Test
  public void testDtoOps() {
    RBucket<GirlDTO> girlDto = redissonClient.getBucket("girl");
    GirlDTO girl = new GirlDTO().setId(1).setAge(2).setCupSize("3");
    girlDto.set(girl);
    print("girlDto: {}", girlDto.get());

    GirlDTO girl2 = new GirlDTO().setId(1).setAge(33).setCupSize("333");
    // compareAndSet不关注hashcode和equals方法，只关注字符串内容
    boolean result = girlDto.compareAndSet(girl2, girl2);
    print("compareAndSet: {}", result);
  }

  @Test
  public void testCompareAndSet() {
    RBucket<GirlDTO> girlDto = redissonClient.getBucket("girl");
    GirlDTO girl = new GirlDTO().setId(1).setAge(2).setCupSize("3");
    girlDto.set(girl);
    print("girlDto: {}", girlDto.get());

    GirlDTO girl2 = new GirlDTO().setId(1).setAge(33).setCupSize("333");
    // compareAndSet不关注hashcode和equals方法，只关注字符串内容
    // expect == null 时退化成 trySet
    // update == null 时退化成 trySet 删除
    boolean success = girlDto.compareAndSet(girl, null);
    print("compareAndSet: {}", success);
    print("compareAndSet result : {}", girlDto.get());
  }

  @Test
  public void testCompareAndSetByWhichContent() {
    RBucket<GirlDTO> girlDto = redissonClient.getBucket("girl");
    GirlDTO girl = new GirlDTO().setId(1).setAge(2).setCupSize("3");
    girlDto.set(girl);
    print("girlDto: {}", girlDto.get());

    GirlDTO girl2 = new GirlDTO().setId(1).setAge(33).setCupSize("333");
    boolean success = girlDto.compareAndSet(girl, girl2);
    print("compareAndSet: {}", success);
    print("compareAndSet result : {}", girlDto.get());
  }

}