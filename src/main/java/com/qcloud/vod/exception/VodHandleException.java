package com.qcloud.vod.exception;

/**
 * @author jianguoxu
 * @time 2017/9/5 10:55
 */
public class VodHandleException extends RuntimeException {

    public VodHandleException() {
        super();
    }

    public VodHandleException(String message, String result) {
        super(message + ", result=" + result);
    }
}
