package com.qcloud.vod.model;

import com.tencentcloudapi.vod.v20180717.models.ApplyUploadRequest;

/**
 * 上传请求
 *
 * @author jianguoxu
 */
public class VodUploadRequest extends ApplyUploadRequest {

    private String MediaFilePath;

    private String CoverFilePath;

    private Integer ConcurrentUploadNumber;

    public VodUploadRequest() {}

    public VodUploadRequest(String mediaFilePath) {
        this.MediaFilePath = mediaFilePath;
    }

    public VodUploadRequest(String mediaFilePath, String coverFilePath) {
        this(mediaFilePath);
        this.CoverFilePath = coverFilePath;
    }

    public VodUploadRequest(String mediaFilePath, String coverFilePath, String procedure) {
        this(mediaFilePath, coverFilePath);
        this.setProcedure(procedure);
    }

    public String getMediaFilePath() {
        return MediaFilePath;
    }

    public void setMediaFilePath(String mediaFilePath) {
        this.MediaFilePath = mediaFilePath;
    }

    public String getCoverFilePath() {
        return CoverFilePath;
    }

    public void setCoverFilePath(String coverFilePath) {
        this.CoverFilePath = coverFilePath;
    }

    public Integer getConcurrentUploadNumber() {
        return ConcurrentUploadNumber;
    }

    public void setConcurrentUploadNumber(Integer concurrentUploadNumber) {
        this.ConcurrentUploadNumber = concurrentUploadNumber;
    }
}
