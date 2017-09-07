package com.qcloud.vod.response;

/**
 * 点播上传请求返回结构
 * @author jianguoxu
 * @time 2017/9/5 10:22
 */
public class VodUploadApplyResponse extends VodBaseResponse {

    //视频信息
    private StorageModel video;

    //封面信息
    private StorageModel cover;

    //appId
    private Integer storageAppId;

    //bucket
    private String storageBucket;

    //region
    private String storageRegion;

    //支持cos v5版对应的region
    private String storageRegionV5;

    //sessionKey
    private String vodSessionKey;

    public StorageModel getVideo() {
        return video;
    }

    public void setVideo(StorageModel video) {
        this.video = video;
    }

    public StorageModel getCover() {
        return cover;
    }

    public void setCover(StorageModel cover) {
        this.cover = cover;
    }

    public Integer getStorageAppId() {
        return storageAppId;
    }

    public void setStorageAppId(Integer storageAppId) {
        this.storageAppId = storageAppId;
    }

    public String getStorageBucket() {
        return storageBucket;
    }

    public void setStorageBucket(String storageBucket) {
        this.storageBucket = storageBucket;
    }

    public String getStorageRegion() {
        return storageRegion;
    }

    public void setStorageRegion(String storageRegion) {
        this.storageRegion = storageRegion;
    }

    public String getStorageRegionV5() {
        return storageRegionV5;
    }

    public void setStorageRegionV5(String storageRegionV5) {
        this.storageRegionV5 = storageRegionV5;
    }

    public String getVodSessionKey() {
        return vodSessionKey;
    }

    public void setVodSessionKey(String vodSessionKey) {
        this.vodSessionKey = vodSessionKey;
    }

    @Override
    public String toString() {
        return "VodUploadApplyResponse{" +
                "video=" + video +
                ", cover=" + cover +
                ", storageAppId=" + storageAppId +
                ", storageBucket='" + storageBucket + '\'' +
                ", storageRegion='" + storageRegion + '\'' +
                ", storageRegionV5='" + storageRegionV5 + '\'' +
                ", vodSessionKey='" + vodSessionKey + '\'' +
                '}';
    }
}
