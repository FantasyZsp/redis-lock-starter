package xyz.mydev.redis.lock.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import xyz.mydev.redis.RootTest;
import xyz.mydev.redis.business.AnnotatedController;
import xyz.mydev.redis.business.GirlDTO;
import xyz.mydev.redis.lock.util.ThreadUtils;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * @author ZSP
 */
@Slf4j
public class AnnotatedControllerTest extends RootTest {
    @Autowired
    private AnnotatedController annotatedController;


    @Test
    public void tryLockCaseInsert() {
        String result = annotatedController.tryLockCaseInsert("test_annotation");
        log.info("执行结果: {}", result);
    }

    @Test
    public void tryLockCaseInsertWithDto() {
        String result = annotatedController.tryLockCaseInsertWithDto(new GirlDTO().setId(1));
        log.info("执行结果: {}", result);
    }

    @Test
    public void tryLockCaseInsertWithDtoMultiThread() {
        Callable<String> task = () -> annotatedController.tryLockCaseInsertWithDto(new GirlDTO().setId(1));

        FutureTask<String> futureTask = new FutureTask<>(task);
        FutureTask<String> futureTask2 = new FutureTask<>(task);
        new Thread(futureTask, "T1-tryLock").start();
        new Thread(futureTask2, "T2-tryLock").start();

        Assertions.assertThrows(ExecutionException.class, () -> {
            log.info("T1执行结果: {}", futureTask.get());
            log.info("T2执行结果: {}", futureTask2.get());
        });

    }

    @Test
    public void reentrantLockTest() throws ExecutionException, InterruptedException {
        Callable<String> task = () -> {
            annotatedController.tryLockCaseInsertWithDto(new GirlDTO().setId(1));
            return annotatedController.tryLockCaseInsertWithDto(new GirlDTO().setId(1));
        };

        FutureTask<String> futureTask = new FutureTask<>(task);
//    FutureTask<String> futureTask2 = new FutureTask<>(task);
        new Thread(futureTask, "T1-tryLock").start();
//    new Thread(futureTask2, "T2-tryLock").start();

        log.info("T1执行结果: {}", futureTask.get());
//    log.info("T2执行结果: {}", futureTask2.get());
    }

    @Test
    public void retrantLock() {
        String result = annotatedController.retrantLock(new GirlDTO().setId(1));
        log.info(result);
    }

    @Test
    public void insertWithUniqueAge() {
        Runnable runnable = () -> annotatedController.insertWithUniqueAge(new GirlDTO().setAge(3));
        new Thread(runnable, "T1").start();
        new Thread(runnable, "T2").start();


        ThreadUtils.join(5000);
    }
}
