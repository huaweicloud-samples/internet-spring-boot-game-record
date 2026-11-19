package com.huaweicloud.scorekeep.web;

import com.huaweicloud.scorekeep.rules.InvalidMoveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** 把业务层抛出的异常映射为明确的 HTTP 语义。 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError("not_found", e.getMessage()));
    }

    @ExceptionHandler({InvalidMoveException.class, IllegalArgumentException.class, NumberFormatException.class})
    public ResponseEntity<ApiError> handleBadRequest(RuntimeException e) {
        log.info("请求被拒绝：{}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(new ApiError("invalid_request", e.getMessage()));
    }

    /** 接口错误响应体。 */
    public record ApiError(String error, String message) {
    }
}
