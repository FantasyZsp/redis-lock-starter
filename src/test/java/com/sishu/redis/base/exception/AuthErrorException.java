package com.sishu.redis.base.exception;

/**
 * @author ZSP
 */
public class AuthErrorException extends RuntimeException {
  private String info;

  public AuthErrorException(AuthError authError, String message) {
    super(message);
    this.info = authError.getInfo();
  }

  public AuthErrorException(AuthError authError) {
    super(authError.getInfo());
    this.info = authError.getInfo();
  }
}
