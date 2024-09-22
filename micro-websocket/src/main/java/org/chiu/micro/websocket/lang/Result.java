package org.chiu.micro.websocket.lang;

import java.util.function.Supplier;


/**
 * @author mingchiuli
 * @create 2021-10-27 3:27 PM
 */
public record Result<T>(

        String msg,

        Integer code,

        T data) {

    public static <T> Result<T> success(T data) {
        return load(200, "success", data);
    }

    public static <T> Result<T> success() {
        return load(200, "success", null);
    }

    private static <T> Result<T> load(Integer code, String msg, T data) {
        return new Result<>(msg, code, data);
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
}
