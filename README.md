## 简介
vod-java-sdk主要用于提供点播相关功能API,辅助用户服务端进行文件上传等功能

## maven依赖
```
<dependency>
    <groupId>com.qcloud</groupId>
    <artifactId>vod_api</artifactId>
    <version>2.1.1</version>
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

## 使用代理上传
```
public static void main(String[] args) {
	HttpProfile httpProfile = new HttpProfile();
	httpProfile.setProxyHost("127.0.0.1");
	httpProfile.setProxyPort(8888);
	// if need authorization
	httpProfile.setProxyUsername("username");
	httpProfile.setProxyPassword("password");

	VodUploadRequest request = new VodUploadRequest("video/Wildlife.mp4", "video/Wildlife-cover.png");
	VodUploadClient client = new VodUploadClient("your secretId","your secretKey", httpProfile);
	try {
		VodUploadResponse response = client.upload("ap-guangzhou", request);
		System.out.println(response.getFileId());
		System.out.println(response.getMediaUrl());
		System.out.println(response.getCoverUrl());
	} catch (Exception e) {
		e.printStackTrace();
	}
}
```
