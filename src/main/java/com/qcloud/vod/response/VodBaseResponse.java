package com.qcloud.vod.response;

/**
 * 点播基础返回结构
 * @author jianguoxu
 * @time 2017/9/4 20:23
 */
public class VodBaseResponse {

    //状态码
    private Integer code;

    //附带信息
    private String message;

    //状态码描述
    private String codeDesc;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCodeDesc() {
        return codeDesc;
    }

    public void setCodeDesc(String codeDesc) {
        this.codeDesc = codeDesc;
    }

    public boolean isSuccess() {
        return code == 0;
    }

    public boolean isFail() {
        return !isSuccess();
    }

    @Override
    public String toString() {
        return "VodBaseResponse{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", codeDesc='" + codeDesc + '\'' +
                '}';
    }
}
