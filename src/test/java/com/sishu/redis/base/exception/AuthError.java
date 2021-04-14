package com.sishu.redis.base.exception;

import com.sishu.redis.lock.support.exception.ExceptionTag;

public enum AuthError implements ExceptionTag {

  EXPIRED("EXPIRED");

  private final String info;

  AuthError(String info) {
    this.info = info;
  }

  public String getInfo() {
    return info;
  }
}