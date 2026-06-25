package com.coursemanager.handler;

import com.coursemanager.utils.CommonResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.HttpMediaTypeNotSupportedException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author hhl
 * @date 2024/06/07 15:59
 * @description 异常统一获取处理返回
 */
@ControllerAdvice
@Slf4j
public class WebExceptionHandler {

    @ResponseBody
    @ExceptionHandler({Exception.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public CommonResult<Void> handleException(Exception exception) {
        log.warn(exception.toString());
        return CommonResult.error(exception.toString());
    }

    @ResponseBody
    @ExceptionHandler({RuntimeException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public CommonResult<Void> handleException(RuntimeException exception) {
        log.warn(exception.toString());
        return CommonResult.error(exception.getMessage());
    }

    /**
     * 处理请求参数格式错误 @RequestBody上使用@Valid 实体上使用@NotNull等，验证失败后抛出的异常是MethodArgumentNotValidException异常
     */
    @ResponseBody
    @SuppressWarnings("null")
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public CommonResult<Void> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException exception) throws JsonProcessingException {
        List<ObjectError> errors = exception.getBindingResult().getAllErrors();
        Map<String, String> map = (Map<String, String>) errors.stream().collect(Collectors.toMap((item) -> item.getCodes()[0],
                (item) -> item.getDefaultMessage()));
        log.warn(exception.toString());
        return CommonResult.error(new ObjectMapper().writeValueAsString(map.values()));
    }

    /**
     * 处理请求 Content-Type 错误（最常见：Postman Body 选成了 Text，应该选 raw + JSON）
     */
    @ResponseBody
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CommonResult<Void> httpMediaTypeNotSupportedExceptionHandler(HttpMediaTypeNotSupportedException exception) {
        log.warn(exception.toString());
        return CommonResult.error("请求 Content-Type 不正确，请使用 application/json");
    }
}
