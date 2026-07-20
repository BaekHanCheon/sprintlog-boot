package com.sprintlog.sprintlogboot.exception;

public class BusinessException extends RuntimeException {

  private final ErrorCode errorCode;

  // 기본메시지 (ErrorCode의 defaultMessage)로 던진다.
  public BusinessException(ErrorCode errorCode) {
    super(errorCode.getDefaultMessage());
    this.errorCode = errorCode;
  }
  //상세메시지를 직접 주어 던진다.
  public BusinessException(ErrorCode errorCode, String detail) {
    super(detail);
    this.errorCode = errorCode;
  }
  //예외변환용 - 저수준의 예외를 감싸 비즈니스 예외로 변환해서 던진다.
  public BusinessException(ErrorCode errorCode, String detail, Throwable cause) {
    super(detail, cause);
    this.errorCode = errorCode;
  }

  public ErrorCode getErrorCode() {
    return errorCode;
  }
}
