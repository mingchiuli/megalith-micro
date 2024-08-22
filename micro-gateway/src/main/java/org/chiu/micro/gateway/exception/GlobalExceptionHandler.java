package org.chiu.micro.gateway.exception;

import lombok.extern.slf4j.Slf4j;

import org.chiu.micro.gateway.lang.Result;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;



/**
 * @author mingchiuli
 * @create 2021-10-27 9:29 PM
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(value = BadCredentialsException.class)
    public Result<Object> handler(BadCredentialsException e) {
        return Result.fail(e.getMessage(), () -> log.error("authentication exception:", e));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public Result<String> handler(MethodArgumentNotValidException e) {
        return e.getBindingResult().getAllErrors().stream()
                .findFirst()
                .<Result<String>>map(error ->
                        Result.fail(error.getDefaultMessage(), () -> log.error("entity validate exception------------", e)))
                .orElseGet(Result::fail);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = IllegalArgumentException.class)
    public Result<String> handler(IllegalArgumentException e) {
        return Result.fail(e.getMessage(), () -> log.error("Assert exception------------", e));
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(value = AccessDeniedException.class)
    public Result<String> handler(AccessDeniedException e){
        return Result.fail(e.getMessage(),  () -> log.error("authorization exception------------", e));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = RuntimeException.class)
    public Result<String> handler(RuntimeException e) {
        return Result.fail(e.getMessage(), () -> log.error("runtime exception------------", e));
    }

}
