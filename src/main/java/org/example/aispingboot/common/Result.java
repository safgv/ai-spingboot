package org.example.aispingboot.common;

import lombok.Data;

@Data
public class Result<T> {
    private String code;
    private String msg;
    private T data;

    public static <T> Result<T> ok() {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMsg(ResultCode.SUCCESS.getMsg());
        return result;
    }

    // 方法重载
    public static <T> Result<T> ok(T data) {
        Result<T> result = ok();  //调用无参构造方法，创建一个Result对象
        result.setData(data);   //把响应数据赋值给Result对象的data属性
        return result;
    }

    public static <T> Result<T> error() {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.ERROR.getCode());
        result.setMsg(ResultCode.ERROR.getMsg());
        return result;
    }

    public static <T> Result<T> error(String code, String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }
}
