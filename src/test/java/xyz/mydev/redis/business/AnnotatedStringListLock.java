package xyz.mydev.redis.business;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.mydev.redis.lock.annotation.RedisLock;

import java.util.*;

/**
 * 配合测试注解
 *
 * @author ZSP
 */
@Slf4j
@Service
public class AnnotatedStringListLock {


    // age -> girlDto
    private static final Map<Integer, GirlDTO> DATABASES = new HashMap<>();

    static {
        DATABASES.put(1, new GirlDTO(1, "1", 1));
        DATABASES.put(2, new GirlDTO(2, "2", 2));
        DATABASES.put(3, new GirlDTO(3, "3", 3));
    }


    @RedisLock(key = "#girls", exceptionMessage = "String test", waitTime = 0, exceptionClass = NullPointerException.class)
    public void multiKey(String head, List<String> girls, String tail) {
        log.info("multiKey invoke...");
    }

    @RedisLock(key = "constantKey", exceptionMessage = "String test", waitTime = 0)
    public void constantKey(String text) {
        log.info("constantKey invoke...");
    }


    @RedisLock(prefix = "222", key = "#girl1.hasId(#girl1.id)", waitTime = 0)
    public String function(GirlDTO girl1) {
        return "success";
    }


    @RedisLock(key = "T(com.sishu.redis.lock.redisson.business.AnnotatedStringListLock).concatKeys(#girls,#head,#tail,':')", exceptionMessage = "String test", waitTime = 0, exceptionClass = NullPointerException.class)
    public void multiConcatKey(String head, List<String> girls, String tail) {
        log.info("multiKey invoke...");
    }

    public static List<String> concatKeys(Collection<String> keysWrapper, String head, String tail, CharSequence charSequence) {
        charSequence = charSequence == null ? "" : charSequence;

        head = head == null ? "" : head;
        tail = tail == null ? "" : tail;

        ArrayList<String> list = new ArrayList<>();
        for (String wrapper : keysWrapper) {
            String key = head + charSequence + wrapper + charSequence + tail;
            list.add(key);
        }
        return list;
    }


}
