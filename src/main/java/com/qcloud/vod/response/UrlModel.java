package com.qcloud.vod.response;

/**
 * @author jianguoxu
 * @time 2017/9/5 16:49
 */
public class UrlModel {

    private String url;

    private String verify_content;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getVerify_content() {
        return verify_content;
    }

    public void setVerify_content(String verify_content) {
        this.verify_content = verify_content;
    }

    @Override
    public String toString() {
        return "UrlModel{" +
                "url='" + url + '\'' +
                ", verify_content='" + verify_content + '\'' +
                '}';
    }
}
