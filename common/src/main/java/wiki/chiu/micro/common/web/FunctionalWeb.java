package wiki.chiu.micro.common.web;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import wiki.chiu.micro.common.exception.BaseException;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.rpc.AuthHttpService;
import wiki.chiu.micro.common.rpc.config.auth.AuthInfo;
import wiki.chiu.micro.common.vo.AuthRpcVo;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class FunctionalWeb {

    private FunctionalWeb() {
    }

    public static String requiredParam(ServerRequest request, String name) {
        String value = request.param(name)
                .orElseThrow(() -> new IllegalArgumentException("Missing required parameter: " + name));
        if (value.isBlank()) {
            throw new IllegalArgumentException("Parameter must not be blank: " + name);
        }
        return value;
    }

    public static <T> T requiredParam(ServerRequest request, String name, Function<String, T> converter) {
        return convert(name, requiredParam(request, name), converter);
    }

    public static <T> T optionalParam(ServerRequest request, String name, T defaultValue,
                                      Function<String, T> converter) {
        return request.param(name)
                .map(value -> convert(name, value, converter))
                .orElse(defaultValue);
    }

    public static <T> T nullableParam(ServerRequest request, String name, Function<String, T> converter) {
        return request.param(name)
                .map(value -> convert(name, value, converter))
                .orElse(null);
    }

    public static <T> T pathVariable(ServerRequest request, String name, Function<String, T> converter) {
        return convert(name, request.pathVariable(name), converter);
    }

    public static String requiredHeader(ServerRequest request, String name) {
        String value = request.headers().firstHeader(name);
        if (value == null) {
            throw new IllegalArgumentException("Missing required header: " + name);
        }
        return value;
    }

    public static MultipartFile multipartFile(ServerRequest request, String name) {
        MultipartHttpServletRequest multipartRequest;
        if (request.servletRequest() instanceof MultipartHttpServletRequest currentRequest) {
            multipartRequest = currentRequest;
        }
        else {
            multipartRequest = new StandardMultipartHttpServletRequest(request.servletRequest());
        }
        MultipartFile file = multipartRequest.getFile(name);
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Missing required multipart file: " + name);
        }
        return file;
    }

    public static Boolean strictBoolean(String value) {
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        throw new IllegalArgumentException("Expected true or false");
    }

    public static <T extends Number> T positive(T value, String name) {
        if (value == null || value.longValue() <= 0) {
            throw new IllegalArgumentException(name + " must be positive");
        }
        return value;
    }

    public static <T extends Number> T nonNegative(T value, String name) {
        if (value == null || value.longValue() < 0) {
            throw new IllegalArgumentException(name + " must not be negative");
        }
        return value;
    }

    public static <T extends Number> T range(T value, long min, long max, String name) {
        if (value == null || value.longValue() < min || value.longValue() > max) {
            throw new IllegalArgumentException(name + " must be between " + min + " and " + max);
        }
        return value;
    }

    public static AuthInfo authInfo(ServerRequest request, AuthHttpService authHttpService) {
        String token = Objects.requireNonNullElse(
                request.headers().firstHeader(HttpHeaders.AUTHORIZATION), "");
        AuthRpcVo authRpcVo = Result.handleResult(() -> authHttpService.getAuthentication(token));
        return AuthInfo.builder()
                .userId(authRpcVo.userId())
                .roles(authRpcVo.roles())
                .authorities(authRpcVo.authorities())
                .build();
    }

    public static ServerResponse ok(Object body) {
        return ServerResponse.ok().body(body);
    }

    public static RouterFunctions.Builder withDefaultErrorHandling(RouterFunctions.Builder builder, Logger log) {
        builder.onError(ConstraintViolationException.class,
                (exception, request) -> error(HttpStatus.BAD_REQUEST, exception, log));
        builder.onError(HttpMessageNotReadableException.class,
                (exception, request) -> error(HttpStatus.BAD_REQUEST, exception, log));
        builder.onError(ServletRequestBindingException.class,
                (exception, request) -> error(HttpStatus.BAD_REQUEST, exception, log));
        builder.onError(MultipartException.class,
                (exception, request) -> error(HttpStatus.BAD_REQUEST, exception, log));
        builder.onError(BindException.class,
                (exception, request) -> error(HttpStatus.BAD_REQUEST, exception, log));
        builder.onError(IllegalArgumentException.class,
                (exception, request) -> error(HttpStatus.BAD_REQUEST, exception, log));
        builder.onError(BaseException.class,
                (exception, request) -> error(HttpStatus.BAD_REQUEST, exception, log));
        builder.onError(Exception.class,
                (exception, request) -> error(HttpStatus.INTERNAL_SERVER_ERROR, exception, log));
        return builder;
    }

    public static ServerResponse error(HttpStatus status, Throwable exception, Logger log) {
        log.error("HTTP handler exception", exception);
        return ServerResponse.status(status)
                .body(Result.fail(status.value(), errorMessage(exception)));
    }

    public static String errorMessage(Throwable exception) {
        if (exception instanceof ConstraintViolationException violationException) {
            return violationException.getConstraintViolations().stream()
                    .map(ConstraintViolation::getMessage)
                    .distinct()
                    .collect(Collectors.joining(","));
        }
        if (exception instanceof BindException bindException) {
            return bindException.getAllErrors().stream()
                    .map(ObjectError::getDefaultMessage)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(","));
        }
        return Objects.requireNonNullElse(exception.getMessage(), exception.getClass().getSimpleName());
    }

    private static <T> T convert(String name, String value, Function<String, T> converter) {
        try {
            return converter.apply(value);
        }
        catch (RuntimeException exception) {
            throw new IllegalArgumentException("Invalid value for parameter " + name + ": " + value, exception);
        }
    }
}
