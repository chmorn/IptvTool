package com.chmorn.base;

import org.springframework.util.StringUtils;

/**
 * @author chmorn
 * @description 通用接口返回类
 * @date 2022/8/31
 **/
public class ResultApi<T> {

    //返回的信息
    private String message;

    //是否成功true或者false
    private Boolean success;

    //返回的数据
    private T data;

    public ResultApi() {
    }

    public ResultApi(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public ResultApi(T data) {
        this.data = data;
        this.success = true;
    }

    public ResultApi(T data, String message) {
        this.data = data;
        this.success = true;
        this.message = message;
    }

    public static ResultApi<?> success() {
        return new ResultApi(true, "操作成功");
    }

    public static ResultApi<?> failure() {
        return new ResultApi(true, "操作失败");
    }

    public static ResultApi<?> success(String msg) {
        if (StringUtils.isEmpty(msg)) {
            msg = "操作成功";
        }
        return new ResultApi(true, msg);
    }

    public static ResultApi<?> failure(String msg) {
        if (StringUtils.isEmpty(msg)) {
            msg = "操作失败";
        }
        return new ResultApi(false, msg);
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

}
