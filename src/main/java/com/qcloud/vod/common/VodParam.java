package com.qcloud.vod.common;

/**
 * 点播参数
 * @author jianguoxu
 * @time 2017/9/5 14:55
 */
public class VodParam {

    //secretId
    private String secretId;

    //secretKey
    private String secretKey;

    //视频路径
    private String videoPath;

    //封面路径
    private String coverPath;

    //任务流
    private String procedure;

    public VodParam() {}

    public VodParam(String secretId, String secretKey, String videoPath, String coverPath, String procedure) {
        this.secretId = secretId;
        this.secretKey = secretKey;
        this.videoPath = videoPath;
        this.coverPath = coverPath;
        this.procedure = procedure;
    }

    public String getSecretId() {
        return secretId;
    }

    public void setSecretId(String secretId) {
        this.secretId = secretId;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public String getCoverPath() {
        return coverPath;
    }

    public void setCoverPath(String coverPath) {
        this.coverPath = coverPath;
    }

    public String getProcedure() {
        return procedure;
    }

    public void setProcedure(String procedure) {
        this.procedure = procedure;
    }

    @Override
    public String toString() {
        return "VodParam{" +
                "secretId='" + secretId + '\'' +
                ", secretKey='" + secretKey + '\'' +
                ", videoPath='" + videoPath + '\'' +
                ", coverPath='" + coverPath + '\'' +
                ", procedure='" + procedure + '\'' +
                '}';
    }
}
