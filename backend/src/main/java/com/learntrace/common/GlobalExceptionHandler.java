package com.learntrace.common;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(BizException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Result<Void> handleBiz(BizException e) {
    return Result.fail(e.getCode(), e.getMessage());
  }

  @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Result<Void> handleValidation(Exception e) {
    return Result.fail(422, e.getMessage());
  }

  @ExceptionHandler(AccessDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public Result<Void> handleDenied(AccessDeniedException e) {
    return Result.fail(403, "没有访问权限");
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public Result<Void> handleOther(Exception e) {
    return Result.fail(500, e.getMessage());
  }
}
