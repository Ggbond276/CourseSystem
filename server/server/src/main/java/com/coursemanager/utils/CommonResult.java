package com.coursemanager.utils;

/**
 * @author hhl
 * @date 2024/06/06 17:45
 * @description 通用返回类
 */
public class CommonResult<T> {
    /**
     * 状态码
     */
    private int code;

    /**
     * 返回数据
     */
    private T data;

    /**
     * 返回信息
     */
    private String msg;

    public CommonResult(int code, T data, String msg) {
        this.code = code;
        this.data = data;
        this.msg = msg;
    }

    public static <T> CommonResult<T> result(int code, T data, String msg) {
        return new CommonResult<>(code, data, msg);
    }

    public static <T> CommonResult<T> success(T data) {
        return result(ResultCode.SUCCESS.getCode(), data, ResultCode.SUCCESS.msg);
    }

    public static <T> CommonResult<T> success(String msg) {
        return result(ResultCode.SUCCESS.getCode(), null,msg);
    }

    public static <T> CommonResult<T> success(T data, String msg) {
        return result(ResultCode.SUCCESS.getCode(), data, msg);
    }

    public static <T> CommonResult<T> fail(String msg) {
        return result(ResultCode.FAILURE.getCode(), null, msg);
    }

    public static <T> CommonResult<T> fail(T data, String msg) {
        return result(ResultCode.FAILURE.getCode(), data, msg);
    }

    public static <T> CommonResult<T> error(String msg) {
        return result(ResultCode.INTERNAL_SERVER_ERROR.getCode(), null, msg);
    }

    public static <T> CommonResult<T> error(T data, String msg) {
        return result(ResultCode.INTERNAL_SERVER_ERROR.getCode(), data, msg);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "CommonResult{" +
                "code=" + code +
                ", data=" + data +
                ", msg='" + msg + '\'' +
                '}';
    }

    /**
     * 返回状态值
     */
    public enum ResultCode {
        // 成功
        SUCCESS(200, "操作成功"),
        // 业务失败
        FAILURE(400, "业务异常"),
        // 认证异常
        UN_AUTH(401, "token不存在或已失效"),
        // 服务器异常
        INTERNAL_SERVER_ERROR(500, "服务器异常");

        final int code;

        final String msg;

        public int getCode() {
            return this.code;
        }

        public String getMessage() {
            return this.msg;
        }

        ResultCode(final int code, final String message) {
            this.code = code;
            this.msg = message;
        }

    }
}
