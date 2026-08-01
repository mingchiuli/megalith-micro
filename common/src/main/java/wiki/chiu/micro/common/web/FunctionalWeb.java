package wiki.chiu.micro.common.web;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
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
        return request.param(name)
                .orElseThrow(() -> new IllegalArgumentException("Missing required parameter: " + name));
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
        if (file == null) {
            throw new IllegalArgumentException("Missing required multipart file: " + name);
        }
        return file;
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

    public static HandlerFilterFunction<ServerResponse, ServerResponse> badRequestErrors(Logger log) {
        return (request, next) -> {
            try {
                return next.handle(request);
            }
            catch (Exception exception) {
                log.error("HTTP handler exception", exception);
                return ServerResponse.badRequest().body(Result.fail(errorMessage(exception)));
            }
        };
    }

    public static String errorMessage(Exception exception) {
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
