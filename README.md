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
public static void main(String[] args) throws Exception {
        VodParam param = new VodParam(
                "your secretId",                //secretId
                "your secretKey",               //secretKey
                "videos/Wildlife.wmv",          //视频路径
                "videos/Wildlife-cover.png",    //封面路径
                null                            //任务流
        );
        VodApi.upload(param);
}
```
