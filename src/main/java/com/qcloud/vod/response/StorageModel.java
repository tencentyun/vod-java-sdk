package com.qcloud.vod.response;

/**
 * 视频模型
 * @author jianguoxu
 * @time 2017/9/5 10:28
 */
public class StorageModel {

    //存储签名
    private String storageSignature;

    //存储路径
    private String storagePath;

    public String getStorageSignature() {
        return storageSignature;
    }

    public void setStorageSignature(String storageSignature) {
        this.storageSignature = storageSignature;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    @Override
    public String toString() {
        return "StorageModel{" +
                "storageSignature='" + storageSignature + '\'' +
                ", storagePath='" + storagePath + '\'' +
                '}';
    }
}
