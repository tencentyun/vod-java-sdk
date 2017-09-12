package com.qcloud.vod.exception;

/**
 * 点播参数错误
 * @author jianguoxu
 * @time 2017/9/4 19:10
 */
public class VodParamException extends RuntimeException {

    public VodParamException() {
        super();
    }

    public VodParamException(String message) {
        super(message);
    }
}
