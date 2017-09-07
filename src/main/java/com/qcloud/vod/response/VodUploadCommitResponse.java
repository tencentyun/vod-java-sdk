package com.qcloud.vod.response;

/**
 * 点播确认上传返回结构
 * @author jianguoxu
 * @time 2017/9/5 16:43
 */
public class VodUploadCommitResponse extends VodBaseResponse {

    //文件id
    private String fileId;

    //视频相关
    private UrlModel video;

    //封面相关
    private UrlModel cover;

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public UrlModel getVideo() {
        return video;
    }

    public void setVideo(UrlModel video) {
        this.video = video;
    }

    public UrlModel getCover() {
        return cover;
    }

    public void setCover(UrlModel cover) {
        this.cover = cover;
    }

    @Override
    public String toString() {
        return "VodUploadCommitResponse{" +
                "fileId='" + fileId + '\'' +
                ", video=" + video +
                ", cover=" + cover +
                '}';
    }
}
