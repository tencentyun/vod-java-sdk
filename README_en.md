![logo](https://main.qcloudimg.com/raw/83708ee18544f80d54c99c6b8ad358fe.jpg)
## Overview
The VOD SDK for Java is an SDK for Java encapsulated based on the upload features of VOD. It provides a rich set of upload capabilities to meet your diversified upload needs. In addition, it encapsulates the APIs of VOD, making it easy for you to integrate the upload capabilities without the need to care about underlying details.

## Features
* [x] General file upload
* [x] HLS file upload
* [x] Upload with cover
* [x] Upload to subapplication
* [x] Upload with task flow
* [x] Upload to specified region
* [x] Upload with temporary key
* [x] Upload with proxy

## Documentation
- [Preparations](https://intl.cloud.tencent.com/document/product/266/33912)
- [API documentation](https://intl.cloud.tencent.com/document/product/266/33914)
- [Feature documentation](https://intl.cloud.tencent.com/document/product/266/33914)
- [Error codes](https://intl.cloud.tencent.com/document/product/266/33914)

## Installation
We recommend you use Maven to install the SDK:
```xml
<dependency>
    <groupId>com.qcloud</groupId>
    <artifactId>vod_api</artifactId>
    <version>2.1.5</version>
</dependency>
```

## Test
The SDK provides a wealth of test cases. You can refer to their call methods. For more information, please see [Test Cases](https://github.com/tencentyun/vod-java-sdk/blob/master/src/test/java/com/qcloud/vod/VodUploadClientTest.java).
You can view the execution of test cases by running the following command:
```xml
mvn test
```

## Release Notes
The changes of each version are recorded in the release notes. For more information, please see [Release Notes](https://github.com/tencentyun/vod-java-sdk/releases).

## Contributors
We appreciate the great support of the following developers to the project, and you are welcome to join us.

<a href="https://github.com/xujianguo"><img width=50 height=50 src="https://avatars1.githubusercontent.com/u/7297536?s=60&v=4" /></a><a href="https://github.com/soulhdb"><img width=50 height=50 src="https://avatars3.githubusercontent.com/u/5770953?s=60&v=4" /></a>

## License
[MIT](https://github.com/tencentyun/vod-java-sdk/blob/master/LICENSE)