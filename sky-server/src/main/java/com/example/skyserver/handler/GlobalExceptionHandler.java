package com.example.skyserver.handler;

import com.example.skyserver.exception.BusinessException;
import com.example.skyserver.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<String> businessException(
            BusinessException e) {

        log.warn("业务异常：{}", e.getMessage());

        return Result.error(e.getMessage());
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> validationException(
            MethodArgumentNotValidException e) {

        String message =
                e.getBindingResult().getFieldError() == null
                        ? "参数校验失败"
                        : e.getBindingResult()
                        .getFieldError()
                        .getDefaultMessage();

        return Result.error(message);
    }
    @ExceptionHandler(BindException.class)
    public Result<String> bindException(BindException e) {
        return Result.error(e.getBindingResult().getFieldError() == null
                ? "参数校验失败"
                : e.getBindingResult().getFieldError().getDefaultMessage());
    }
    @ExceptionHandler(Exception.class)
    public Result<String> unexpectedException(Exception e) {
        log.error("未处理的请求异常", e);
        return Result.error("服务器内部异常");
    }
}
