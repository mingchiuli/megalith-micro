package org.chiu.micro.user.lang;

import java.util.function.Supplier;

/**
 * @author mingchiuli
 * @create 2021-10-27 3:27 PM
 */
public class Result<T> {

    private String msg;

    private Integer code;

    private T data;

    public Result() {
    }

    public static <T> Result<T> success(T data) {
        return load(200, "success", data);
    }

    public static <T> Result<T> success() {
        return load(200, "success", null);
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

    public String getMsg() {
        return this.msg;
    }

    public Integer getCode() {
        return this.code;
    }

    public T getData() {
        return this.data;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof Result)) return false;
        final Result<?> other = (Result<?>) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$msg = this.getMsg();
        final Object other$msg = other.getMsg();
        if (this$msg == null ? other$msg != null : !this$msg.equals(other$msg)) return false;
        final Object this$code = this.getCode();
        final Object other$code = other.getCode();
        if (this$code == null ? other$code != null : !this$code.equals(other$code)) return false;
        final Object this$data = this.getData();
        final Object other$data = other.getData();
        if (this$data == null ? other$data != null : !this$data.equals(other$data)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof Result;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $msg = this.getMsg();
        result = result * PRIME + ($msg == null ? 43 : $msg.hashCode());
        final Object $code = this.getCode();
        result = result * PRIME + ($code == null ? 43 : $code.hashCode());
        final Object $data = this.getData();
        result = result * PRIME + ($data == null ? 43 : $data.hashCode());
        return result;
    }

    public String toString() {
        return "Result(msg=" + this.getMsg() + ", code=" + this.getCode() + ", data=" + this.getData() + ")";
    }
}
