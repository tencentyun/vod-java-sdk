## 简介
vod-java-sdk主要用于提供点播相关功能API,辅助用户服务端进行文件上传等功能

## maven依赖
```
<dependency>
    <groupId>com.qcloud</groupId>
    <artifactId>vod_api</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 上传例子
```
public static void main(String[] args) {
        try {
            VodApi vodApi = new VodApi("your secretId", "your secretKey");
            //设置签名过期时长
            //VodApi vodApi = new VodApi("your secretId", "your secretKey", 24 * 3600);
            vodApi.upload("videos/Wildlife.wmv", "videos/Wildlife-cover.png");
        } catch(Exception e) {
            //打日志
            log.error("上传视频失败", e)
        }
}
```
