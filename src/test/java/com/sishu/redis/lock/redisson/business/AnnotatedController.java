package com.sishu.redis.lock.redisson.business;

import com.sishu.redis.lock.annotation.RedisLock;
import com.sishu.redis.lock.redisson.GirlDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ZSP
 */
@Slf4j
@RestController
@RequestMapping("/annotaion/redis-lock")
public class AnnotatedController {
  private final AnnotatedService annotatedService;

  public AnnotatedController(AnnotatedService annotatedService) {

    this.annotatedService = annotatedService;
  }


  @RedisLock(route = "redis-lock", key = "#uniqueName", waitTime = 2000)
  @PostMapping("/try-lock")
  public String tryLockCaseInsert(@RequestParam("name") String uniqueName) {
    return uniqueName + " 插入成功！";
  }

  @RedisLock(route = "redis-lock", key = "#girlDTO.id", waitTime = 0, exceptionClass = IllegalArgumentException.class, exceptionMessage = "xxx")
  @PostMapping("/try-lock-dto")
  public String tryLockCaseInsertWithDto(@RequestBody GirlDTO girlDTO) {
    return girlDTO + " 插入成功！";
  }

  @RedisLock(route = "redis-lock", key = "#girlDTO.id", waitTime = 2000)
  @PostMapping("/retrant-lock")
  public String retrantLock(@RequestBody GirlDTO girlDTO) {
    return annotatedService.lockWithDto(girlDTO);
  }


  @PostMapping("/save-with-unique-age")
  public void insertWithUniqueAge(GirlDTO girl) {
    Assert.notNull(girl.getAge(), "age must not be null");
    annotatedService.insertWithUniqueAge(girl);
  }

}
