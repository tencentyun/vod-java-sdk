package com.qcloud.vod.common;

/**
 * 点播Cos配置
 * @author jianguoxu
 * @time 2017/9/5 15:35
 */
public class VodCosConf {

    private String bucketName;

    private String storagePath;

    private String filePath;

    public VodCosConf() {}

    public VodCosConf(String bucketName, String storagePath, String filePath) {
        this.bucketName = bucketName;
        this.storagePath = storagePath;
        this.filePath = filePath;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public String toString() {
        return "VodCosConf{" +
                "bucketName='" + bucketName + '\'' +
                ", storagePath='" + storagePath + '\'' +
                ", filePath='" + filePath + '\'' +
                '}';
    }
}
