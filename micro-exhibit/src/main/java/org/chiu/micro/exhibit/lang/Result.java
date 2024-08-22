package org.chiu.micro.exhibit.lang;

import java.util.function.Supplier;

import org.chiu.micro.exhibit.exception.MissException;
import static org.chiu.micro.exhibit.lang.ExceptionMessage.*;

import lombok.Data;
import java.util.Objects;

/**
 * @author mingchiuli
 * @create 2021-10-27 3:27 PM
 */
@Data
public class Result<T> {

    private String msg;

    private Integer code;

    private T data;

    public static <T> Result<T> success(T data) {
        return load(200, "success",data);
    }

    public static <T> Result<T> success() {
        return load(200, "success",null);
    }

    private static <T> Result<T> load(Integer code, String msg, T data) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setData(data);
        r.setMsg(msg);
        return r;
    }
    public static <T> Result<T> fail(String msg, T data) {
        return load(400, msg, data);
    }

    public static <T> Result<T> fail() {
        return load(400, null, null);
    }

    public static <T> Result<T> fail(String msg) {
        return load(400, msg, null);
    }

    public static <T> Result<T> fail(Integer code, String msg) {
        return load(code, msg, null);
    }

    public static Result<Void> success(Runnable runnable) {
        runnable.run();
        return success();
    }

    public static <T> Result<T> success(Supplier<T> supplier) {
        return Result.success(supplier.get());
    }

    public static <T> Result<T> fail(String msg, Runnable runnable) {
        runnable.run();
        return fail(msg);
    }

    public static <T> Result<T> fail(Integer code, String msg, Runnable runnable) {
        runnable.run();
        return fail(code, msg);
    }

    public T getData() {
        if (Objects.isNull(data)) {
            throw new MissException(NO_FOUND.getMsg());
        }
        return data;
    }
}
