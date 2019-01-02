package com.qcloud.vod.exception;

/**
 * 客户端错误
 *
 * @author jianguoxu
 */
public class VodClientException extends Exception {

    public VodClientException(String message, Throwable t) {
        super(message, t);
    }

    public VodClientException(String message) {
        super(message);
    }

    public VodClientException(Throwable t) {
        super(t);
    }
}
