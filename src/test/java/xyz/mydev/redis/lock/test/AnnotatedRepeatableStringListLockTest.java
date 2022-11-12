package xyz.mydev.redis.lock.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import xyz.mydev.redis.RootTest;
import xyz.mydev.redis.business.AnnotatedRepeatableStringListLock;

import java.util.List;

/**
 * @author ZSP
 */
public class AnnotatedRepeatableStringListLockTest extends RootTest {

    @Autowired
    private AnnotatedRepeatableStringListLock annotatedRepeatableStringListLock;


    @Test
    public void test() {
        annotatedRepeatableStringListLock.annotatedRepeatableTest("head", List.of("1", "222222222", "33333333333333"), "tail");
        System.out.println();
    }

}
