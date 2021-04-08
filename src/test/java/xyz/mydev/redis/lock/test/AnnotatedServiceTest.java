package xyz.mydev.redis.lock.test;

import xyz.mydev.redis.RootTest;
import xyz.mydev.redis.lock.redisson.GirlDTO;
import xyz.mydev.redis.lock.redisson.business.AnnotatedService;
import xyz.mydev.redis.lock.util.ThreadUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author ZSP
 */
@Slf4j
public class AnnotatedServiceTest extends RootTest {
  @Autowired
  private AnnotatedService annotatedService;
  private static final Map<Integer, GirlDTO> TEMP_DATABASES = new HashMap<>();

  static {
    TEMP_DATABASES.put(1, new GirlDTO(1, "1", 1));
    TEMP_DATABASES.put(2, new GirlDTO(2, "2", 2));
    TEMP_DATABASES.put(3, new GirlDTO(3, "3", 3));
  }

  @Test
  public void testMultiKey() {
    TEMP_DATABASES.put(4, new GirlDTO(4, "3", null));
    annotatedService.multiKey(List.copyOf(TEMP_DATABASES.values()));
  }

  @Test
  public void testMultiKeyConcurrentOrdered() {
    TEMP_DATABASES.put(4, new GirlDTO(4, "3", null));
    Runnable runnable = () -> annotatedService.multiKey(List.copyOf(TEMP_DATABASES.values()));
    ThreadUtils.startAndJoin(new Thread(runnable, "T1"), 200);
    ThreadUtils.startAndJoin(new Thread(runnable, "T2"), 200);
    ThreadUtils.startAndJoin(new Thread(runnable, "T3"), 200);
    ThreadUtils.startAndJoin(new Thread(runnable, "T4"), 200);
    ThreadUtils.startAndJoin(new Thread(runnable, "T5"), 200);
  }

  @Test
  public void testMultiKeyConcurrent() {
    TEMP_DATABASES.put(4, new GirlDTO(4, "3", null));
    Runnable runnable = () -> annotatedService.multiKey(List.copyOf(TEMP_DATABASES.values()));

    ThreadUtils.start(new Thread(runnable, "T1"));
    ThreadUtils.start(new Thread(runnable, "T2"));
    ThreadUtils.start(new Thread(runnable, "T3"));
    ThreadUtils.start(new Thread(runnable, "T4"));
    ThreadUtils.start(new Thread(runnable, "T5"));

    ThreadUtils.join(10000);
  }

  @Test
  public void testMultiKeyConcurrent2() {
    TEMP_DATABASES.put(4, new GirlDTO(4, "3", null));

    ExecutorService executorService = Executors.newFixedThreadPool(5);
    Runnable runnable = () -> annotatedService.multiKey(List.copyOf(TEMP_DATABASES.values()));

    executorService.submit(runnable);
    executorService.submit(runnable);
    executorService.submit(runnable);
    executorService.submit(runnable);
    executorService.submit(runnable);


    try {
      executorService.awaitTermination(10000, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testMultiKeyWithEmptyList() {
    annotatedService.multiKey(new ArrayList<>());
  }

  @Test
  public void testMultiKeyWithNullList() {
    annotatedService.multiKey(null);
  }

  @Test
  public void testSpelConcat() {
    annotatedService.keyConcat(new GirlDTO().setId(1), new GirlDTO().setId(2));
  }

  @Test
  public void testLockBusinessError() {

    CyclicBarrier start = new CyclicBarrier(5);
    CyclicBarrier end = new CyclicBarrier(5);

    Runnable runnable = () -> {
      try {
        log.info("await...");
        start.await();
        log.info("start...");
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (BrokenBarrierException e) {
        e.printStackTrace();
      }
      annotatedService.lockBusinessError("test_error");
    };
    ThreadUtils.start(new Thread(runnable, "T1"));
    ThreadUtils.start(new Thread(runnable, "T2"));
    ThreadUtils.start(new Thread(runnable, "T3"));
    ThreadUtils.start(new Thread(runnable, "T4"));
    ThreadUtils.start(new Thread(runnable, "T5"));
    ThreadUtils.sleepSeconds(30000);

  }

  @Test
  public void testLockBusinessError2Release() {

    CyclicBarrier start = new CyclicBarrier(5);
    CyclicBarrier end = new CyclicBarrier(5);

    Runnable runnable = () -> {
      try {
        log.info("await...");
        start.await();
        log.info("start...");
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (BrokenBarrierException e) {
        e.printStackTrace();
      }
      annotatedService.tryLockWithRelease("test_error");
    };
    ThreadUtils.start(new Thread(runnable, "T1"));
    ThreadUtils.start(new Thread(runnable, "T2"));
    ThreadUtils.start(new Thread(runnable, "T3"));
    ThreadUtils.start(new Thread(runnable, "T4"));
    ThreadUtils.start(new Thread(runnable, "T5"));
    ThreadUtils.sleepSeconds(30000);

  }

  @Test
  public void lockWithRelease() {

    CyclicBarrier start = new CyclicBarrier(5);
    CyclicBarrier end = new CyclicBarrier(5);

    Runnable runnable = () -> {
      try {
        log.info("await...");
        start.await();
        log.info("start...");
      } catch (InterruptedException | BrokenBarrierException e) {
        e.printStackTrace();
      }
      annotatedService.lockWithRelease("lockWithRelease");
    };
    ThreadUtils.start(new Thread(runnable, "T1"));
    ThreadUtils.start(new Thread(runnable, "T2"));
    ThreadUtils.start(new Thread(runnable, "T3"));
    ThreadUtils.start(new Thread(runnable, "T4"));
    ThreadUtils.start(new Thread(runnable, "T5"));
    ThreadUtils.sleepSeconds(30000);

  }
}
