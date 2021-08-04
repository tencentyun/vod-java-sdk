package com.qcloud.vod.model;

/**
 * Url上传请求
 *
 * @author alanyfwu
 */
public class VodUrlUploadRequest extends VodUploadRequest {

    private String mediaUrl;

    private String coverUrl;

    /**
     * 请求资源的超时时间 单位:秒
     */
    private int timeout = 10;

    public VodUrlUploadRequest(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public VodUrlUploadRequest(String mediaUrl, String coverUrl) {
        this.mediaUrl = mediaUrl;
        this.coverUrl = coverUrl;
    }

    public VodUrlUploadRequest(String mediaUrl, int timeout) {
        this.mediaUrl = mediaUrl;
        this.timeout = timeout;
    }

    public VodUrlUploadRequest(String mediaUrl, String coverUrl, int timeout) {
        this.mediaUrl = mediaUrl;
        this.coverUrl = coverUrl;
        this.timeout = timeout;
    }

    public String getMediaUrl() {
        return this.mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getCoverUrl() {
        return this.coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public int getTimeout() {
        return this.timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

}
