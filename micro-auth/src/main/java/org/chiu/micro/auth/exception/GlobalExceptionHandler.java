package org.chiu.micro.auth.exception;

import org.chiu.micro.common.exception.AuthException;
import org.chiu.micro.common.exception.BaseException;
import org.chiu.micro.common.lang.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
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
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(value = BadCredentialsException.class)
    public Result<Void> handler(BadCredentialsException e) {
        log.error("-------------------error", e);
        return Result.fail(e.getMessage());
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(value = AuthException.class)
    public Result<Void> handler(AuthException e) {
        log.error("-------------------error", e);
        return Result.fail(e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = BaseException.class)
    public Result<Void> handler(BaseException e) {
        log.error("-------------------error", e);
        return Result.fail(e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public Result<Void> handler(MethodArgumentNotValidException e) {
        log.error("-------------------error", e);
        return Result.fail(e.getBindingResult().getAllErrors().stream()
                .findFirst()
                .<String>map(MessageSourceResolvable::getDefaultMessage).orElse(""));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = IllegalArgumentException.class)
    public Result<Void> handler(IllegalArgumentException e) {
        log.error("-------------------error", e);
        return Result.fail(e.getMessage());
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(value = AccessDeniedException.class)
    public Result<Void> handler(AccessDeniedException e) {
        log.error("-------------------error", e);
        return Result.fail(e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = RuntimeException.class)
    public Result<Void> handler(RuntimeException e) {
        log.error("-------------------error", e);
        return Result.fail(e.getMessage());
    }

}
