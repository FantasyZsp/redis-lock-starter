package xyz.mydev.redis.lock.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import xyz.mydev.redis.RootTest;
import xyz.mydev.redis.business.AnnotatedStringListLock;
import xyz.mydev.redis.business.GirlDTO;

import java.util.List;

/**
 * @author ZSP
 */
public class StringTest extends RootTest {

    @Autowired
    private AnnotatedStringListLock annotatedStringListLock;


    @Test
    public void test() {
        annotatedStringListLock.multiKey("head", List.of("1", "222222222", "33333333333333"), "tail");
        System.out.println();
    }

    @Test
    public void testFunction() {
        annotatedStringListLock.function(new GirlDTO(2, "cupSize", 1));
        System.out.println();
    }

    @Test
    public void testMultiConcatKey() {
        annotatedStringListLock.multiConcatKey("head", List.of("1", "222222222", "33333333333333"), "tail");
        System.out.println();
    }

    @Test
    public void testConstantKey() {
        annotatedStringListLock.constantKey("test");
        System.out.println();
    }
}
