## 简介
vod-java-sdk主要用于提供点播相关功能API,辅助用户服务端进行文件上传等功能

## maven依赖
```
<dependency>
    <groupId>com.qcloud</groupId>
    <artifactId>vod_api</artifactId>
    <version>2.1.0</version>
</dependency>
```

## 上传例子
```
public class Main {
    public static void main(String[] args) {
        VodUploadRequest request = new VodUploadRequest("video/Wildlife.mp4", "video/Wildlife-Cover.mp4");
        VodUploadClient client = new VodUploadClient("your secretId", "your secretKey");
        try {
            VodUploadResponse response = client.upload("ap-guangzhou", request);
            System.out.println(response.getFileId());
            System.out.println(response.getMediaUrl());
            System.out.println(response.getCoverUrl());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```
