package xyz.mydev.redis.business;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.mydev.redis.ThreadUtils;
import xyz.mydev.redis.lock.annotation.RedisLock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 配合测试注解
 *
 * @author ZSP
 */
@Slf4j
@Service
public class AnnotatedService {
    // age -> girlDto
    private static final Map<Integer, GirlDTO> DATABASES = new HashMap<>();

    static {
        DATABASES.put(1, new GirlDTO(1, "1", 1));
        DATABASES.put(2, new GirlDTO(2, "2", 2));
        DATABASES.put(3, new GirlDTO(3, "3", 3));
    }

    @RedisLock(prefix = "redis-lock", key = "#uniqueName", waitTime = 2000)
    public String lock(String uniqueName) {
        return uniqueName + " 插入成功！";
    }

    @RedisLock(prefix = "redis-lock", key = "#girlDTO.id", waitTime = 2000)
    public String lockWithDto(GirlDTO girlDTO) {
        return girlDTO + " 插入成功！";
    }

    @RedisLock(key = "#girl.age", exceptionMessage = "已经存在同年龄的记录", waitTime = 0)
    public void insertWithUniqueAge(GirlDTO girl) {
        if (DATABASES.containsKey(girl.getId())) {
            throw new RuntimeException("已经存在同年龄的记录");
        }
        ThreadUtils.join(1000);
        DATABASES.put(girl.getAge(), girl);
    }

    @RedisLock(key = "#girls.![age]", exceptionMessage = "multiKey test", waitTime = 0, exceptionClass = NullPointerException.class)
    public void multiKey(List<GirlDTO> girls) {
        log.info("multiKey invoke...");
    }

    @RedisLock(prefix = "redis-lock", key = "T(java.lang.String).valueOf(#girl1.id).concat(':').concat(#girl2.id)", waitTime = 0)
    public String keyConcat(GirlDTO girl1, GirlDTO girl2) {
        ThreadUtils.join(100);
        return "success";
    }


    @RedisLock(prefix = "redis-lock-bu-error", key = "#uniqueName", waitTime = 100)
    public String lockBusinessError(String uniqueName) {
        if (new Random().nextInt(4) > 2) {
            throw new RuntimeException("error");
        }
        ThreadUtils.join(100);
        return uniqueName + " lock success";
    }

    @RedisLock(prefix = "tryLockWithRelease", key = "#uniqueName", waitTime = 100, leaseTime = 50)
    public String tryLockWithRelease(String uniqueName) {
        log.info("{} entrance business", Thread.currentThread().getName());
        ThreadUtils.join(100);
        return uniqueName + " lock success";
    }

    @RedisLock(prefix = "lockWithRelease", key = "#uniqueName", leaseTime = 50)
    public String lockWithRelease(String uniqueName) {
        log.info("{} entrance business", Thread.currentThread().getName());
        ThreadUtils.join(100);
        return uniqueName + " lock success";
    }

}
