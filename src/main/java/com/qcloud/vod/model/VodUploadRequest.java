package com.qcloud.vod.model;

import com.qcloud.cos.internal.Constants;
import com.tencentcloudapi.vod.v20180717.models.ApplyUploadRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 上传请求
 *
 * @author jianguoxu
 */
public class VodUploadRequest extends ApplyUploadRequest {

    /**
     * 文件路径
     */
    private String mediaFilePath;

    /**
     * 封面路径(可选)
     */
    private String coverFilePath;

    /**
     * 上传并发线程数
     */
    private int concurrentUploadNumber;

    /**
     * 文件达到多大才开始分片上传(单位:byte,默认5mb)
     */
    private long multipartUploadThreshold = 5 * Constants.MB;

    /**
     * 分片时每一片的大小(单位:byte,默认5mb)
     */
    private long minimumUploadPartSize = 5 * Constants.MB;

    /**
     * 是否启用Https上传
     */
    private boolean secureUpload = true;

    /**
     * 自定义请求头
     */
    private Map<String, String> headersMap;

    public VodUploadRequest() {
    }

    public VodUploadRequest(String mediaFilePath) {
        this.mediaFilePath = mediaFilePath;
    }

    public VodUploadRequest(String mediaFilePath, String coverFilePath) {
        this(mediaFilePath);
        this.coverFilePath = coverFilePath;
    }

    public VodUploadRequest(String mediaFilePath, String coverFilePath, String procedure) {
        this(mediaFilePath, coverFilePath);
        this.setProcedure(procedure);
    }

    public String getMediaFilePath() {
        return this.mediaFilePath;
    }

    public void setMediaFilePath(String mediaFilePath) {
        this.mediaFilePath = mediaFilePath;
    }

    public String getCoverFilePath() {
        return this.coverFilePath;
    }

    public void setCoverFilePath(String coverFilePath) {
        this.coverFilePath = coverFilePath;
    }

    public int getConcurrentUploadNumber() {
        return this.concurrentUploadNumber;
    }

    public void setConcurrentUploadNumber(int concurrentUploadNumber) {
        this.concurrentUploadNumber = concurrentUploadNumber;
    }

    public long getMultipartUploadThreshold() {
        return this.multipartUploadThreshold;
    }

    public void setMultipartUploadThreshold(long multipartUploadThreshold) {
        this.multipartUploadThreshold = multipartUploadThreshold;
    }

    public long getMinimumUploadPartSize() {
        return this.minimumUploadPartSize;
    }

    public void setMinimumUploadPartSize(long minimumUploadPartSize) {
        this.minimumUploadPartSize = minimumUploadPartSize;
    }

    public boolean secureUpload() {
        return this.secureUpload;
    }

    public void disableSecureUpload() {
        this.secureUpload = false;
    }

    public void putRequestHeader(String name, String value) {
        if (this.headersMap == null) {
            this.headersMap = new HashMap<>();
        }
        this.headersMap.put(name, value);
    }

    public Map<String, String> getRequestHeader() {
        if (this.headersMap != null) {
            return Collections.unmodifiableMap(this.headersMap);
        }
        return null;
    }

}
