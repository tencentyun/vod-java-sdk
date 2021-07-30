package com.qcloud.vod;

import com.qcloud.vod.common.CopyUtil;
import com.qcloud.vod.common.FileUtil;
import com.qcloud.vod.common.PrintUtil;
import com.qcloud.vod.common.StringUtil;
import com.qcloud.vod.exception.VodClientException;
import com.qcloud.vod.model.VodUploadRequest;
import com.qcloud.vod.model.VodUploadResponse;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.sts.v20180813.StsClient;
import com.tencentcloudapi.sts.v20180813.models.Credentials;
import com.tencentcloudapi.sts.v20180813.models.GetFederationTokenRequest;
import com.tencentcloudapi.sts.v20180813.models.GetFederationTokenResponse;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * VOD upload test case
 *
 * @author jianguoxu
 */
public class VodUploadClientTest {

    public static String secretId = System.getenv("SECRET_ID");
    public static String secretKey = System.getenv("SECRET_KEY");
    private static final Logger logger = LoggerFactory.getLogger(VodUploadClientTest.class);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public VodUploadClient initVodUploadClient() {
        return new VodUploadClient(secretId, secretKey);
    }

    public VodUploadClient initVodUploadClientCustomHttpProfile(HttpProfile httpProfile) {
        return new VodUploadClient(secretId, secretKey, httpProfile);
    }

    /**
     * 获取cos上传的临时凭证
     */
    public Credentials obtainTemporaryCredentials() throws Exception {
        Credentials credentials;
        try {
            Credential cred = new Credential(secretId, secretKey);
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("sts.tencentcloudapi.com");
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            StsClient client = new StsClient(cred, "ap-chengdu", clientProfile);
            GetFederationTokenRequest req = new GetFederationTokenRequest();
            req.setName("customName");
            req.setPolicy("{\"version\": \"2.0\",\"statement\": [{\"effect\": \"allow\",\"resource\": \"*\"}]}");
            req.setDurationSeconds(1800);
            GetFederationTokenResponse resp = client.GetFederationToken(req);
            System.out.println(GetFederationTokenResponse.toJsonString(resp));
            credentials = resp.getCredentials();
        } catch (TencentCloudSDKException e) {
            System.out.println(e.toString());
            throw e;
        }
        return credentials;
    }

    public VodUploadClient initSTSVodUploadClient() throws Exception {
        Credentials credentials = this.obtainTemporaryCredentials();
        return new VodUploadClient(credentials.getTmpSecretId(), credentials.getTmpSecretKey(), credentials.getToken());
    }

    @Test
    public void lackMediaPath() throws Exception {
        thrown.expect(VodClientException.class);
        thrown.expectMessage("lack media path");
        VodUploadRequest request = new VodUploadRequest();
        VodUploadClient client = initVodUploadClient();
        client.upload("ap-guangzhou", request);
    }

    @Test
    public void lackMediaType() throws Exception {
        thrown.expect(VodClientException.class);
        thrown.expectMessage("lack media type");
        VodUploadRequest request = new VodUploadRequest();
        request.setMediaFilePath("video/Wildlife");
        VodUploadClient client = initVodUploadClient();
        client.upload("ap-guangzhou", request);
    }

    @Test
    public void invalidMediaPath() throws Exception {
        thrown.expect(VodClientException.class);
        thrown.expectMessage("media path is invalid");
        VodUploadRequest request = new VodUploadRequest();
        request.setMediaFilePath("video/WildlifeA");
        VodUploadClient client = initVodUploadClient();
        client.upload("ap-guangzhou", request);
    }

    @Test
    public void invalidCoverPath() throws Exception {
        thrown.expect(VodClientException.class);
        thrown.expectMessage("cover path is invalid");
        VodUploadRequest request = new VodUploadRequest();
        request.setMediaFilePath("video/Wildlife.mp4");
        request.setCoverFilePath("video/Wildlife-CoverA");
        VodUploadClient client = initVodUploadClient();
        client.upload("ap-guangzhou", request);
    }

    @Test
    public void lackCoverType() throws Exception {
        thrown.expect(VodClientException.class);
        thrown.expectMessage("lack cover type");
        VodUploadRequest request = new VodUploadRequest();
        request.setMediaFilePath("video/Wildlife.mp4");
        request.setCoverFilePath("video/Wildlife-Cover");
        VodUploadClient client = initVodUploadClient();
        client.upload("ap-guangzhou", request);
    }

    @Test
    public void invalidMediaType() throws Exception {
        thrown.expect(TencentCloudSDKException.class);
        thrown.expectMessage("InvalidParameterValue.MediaType-invalid media type");
        VodUploadRequest request = new VodUploadRequest();
        request.setMediaFilePath("video/Wildlife.mp4");
        request.setMediaType("test");
        VodUploadClient client = initVodUploadClient();
        client.upload("ap-guangzhou", request);
    }

    @Test
    public void invalidCoverType() throws Exception {
        thrown.expect(TencentCloudSDKException.class);
        thrown.expectMessage("InvalidParameterValue.CoverType-invalid cover type");
        VodUploadRequest request = new VodUploadRequest();
        request.setMediaFilePath("video/Wildlife.mp4");
        request.setCoverFilePath("video/Wildlife-Cover.png");
        request.setCoverType("test");
        VodUploadClient client = initVodUploadClient();
        client.upload("ap-guangzhou", request);
    }

    @Test
    public void invalidNoFileName() throws Exception {
        VodUploadRequest request = new VodUploadRequest();
        request.setMediaFilePath("video/.mp4");
        VodUploadClient client = initVodUploadClient();
        client.upload("ap-guangzhou", request);
    }

    @Test
    public void uploadMedia() throws Exception {
        VodUploadRequest request =
                new VodUploadRequest("video/Wildlife.mp4", "video/Wildlife-Cover.png");
        request.setStorageRegion("ap-chongqing");
        request.setMediaName("test-20181129-1423");
        VodUploadClient client = initVodUploadClient();
        VodUploadResponse response = client.upload("ap-guangzhou", request);
        logger.info("Upload FileId = {}", response.getFileId());
    }
    
    @Test
    public void uploadHls() throws Exception {
        VodUploadRequest request = new VodUploadRequest("video/hls/prog_index.m3u8", "");
        VodUploadClient client = initVodUploadClient();
        VodUploadResponse response = client.upload("ap-guangzhou", request);
        logger.info("Upload FileId = {}", response.getFileId());
    }
    
    @Test
    public void uploadMasterPlaylist() throws Exception {
        VodUploadRequest request = new VodUploadRequest("video/hls/bipbopall.m3u8", "");
        VodUploadClient client = initVodUploadClient();
        VodUploadResponse response = client.upload("ap-guangzhou", request);
        logger.info("Upload FileId = {}", response.getFileId());
    }

    @Test
    public void uploadWithSecurityCos() throws Exception {
        VodUploadRequest request = new VodUploadRequest("video/Wildlife.mp4");
        request.openSecureUpload();
        VodUploadClient client = initVodUploadClient();
        VodUploadResponse response = client.upload("ap-guangzhou", request);
        logger.info("Upload FileId = {}", response.getFileId());
    }

    @Test
    public void uploadWithConcurrentUploadNumber() throws Exception {
        VodUploadRequest request = new VodUploadRequest("video/Wildlife.mp4");
        request.setConcurrentUploadNumber(10);
        VodUploadClient client = initVodUploadClient();
        VodUploadResponse response = client.upload("ap-guangzhou", request);
        logger.info("Upload FileId = {}", response.getFileId());
    }

    @Test
    public void uploadByCustomHeader() throws Exception {
        VodUploadRequest request = new VodUploadRequest("video/Wildlife.mp4");
        request.putRequestHeader("header-name","header-value");
        VodUploadClient client = initVodUploadClient();
        VodUploadResponse response = client.upload("ap-guangzhou", request);
        logger.info("Upload FileId = {}", response.getFileId());
    }

    @Test
    public void deferProxyPort() throws Exception {
        thrown.expect(TencentCloudSDKException.class);
        thrown.expectMessage("java.net.UnknownHostException-@.noHostUrl.noHostUrl");
        VodUploadRequest request = new VodUploadRequest("video/Wildlife.mp4");
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setProxyHost("@.noHostUrl.noHostUrl");
        VodUploadClient client = initVodUploadClientCustomHttpProfile(httpProfile);
        VodUploadResponse response = client.upload("ap-guangzhou", request);
        logger.info("Upload FileId = {}", response.getFileId());
    }

    @Test
    public void customHttpProfileUpload() throws Exception {
        VodUploadRequest request = new VodUploadRequest("video/Wildlife.mp4");
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setProtocol("http://");
        VodUploadClient client = initVodUploadClientCustomHttpProfile(httpProfile);
        client.setRetryCount(10);
        if (client.getRetryCount() != 10) {
            logger.error("retryCount error");
        }
        VodUploadResponse response = client.upload("ap-guangzhou", request);
        logger.info("Upload FileId = {}", response.getFileId());
    }

    @Test
    public void customRetryCount() throws Exception {
        VodUploadRequest request = new VodUploadRequest("video/Wildlife.mp4");
        VodUploadClient client = initVodUploadClient();
        client.setRetryCount(10);
        VodUploadResponse response = client.upload("ap-guangzhou", request);
        if (client.getRetryCount() != 10) {
            logger.error("retryCount error");
        }
        logger.info("Upload FileId = {}", response.getFileId());
    }

    @Test
    public void subApplication() throws Exception {
        thrown.expect(TencentCloudSDKException.class);
        thrown.expectMessage("InvalidParameterValue.SubAppId-invalid subappid");
        VodUploadRequest request = new VodUploadRequest("video/Wildlife.mp4");
        request.setSubAppId(13008543630L);
        VodUploadClient client = initVodUploadClient();
        VodUploadResponse response = client.upload("ap-guangzhou", request);
        logger.info("Upload FileId = {}", response.getFileId());
    }

    @Test
    public void temporarySTSClient() throws Exception {
        VodUploadRequest request = new VodUploadRequest("video/Wildlife.mp4");
        request.putRequestHeader("header-name","header-value");
        VodUploadClient client = initSTSVodUploadClient();
        VodUploadResponse response = client.upload("ap-guangzhou", request);
        logger.info("Upload FileId = {}", response.getFileId());
    }

    @Test
    public void uploadAutoStartProcedure() throws Exception {
        VodUploadRequest request = new VodUploadRequest("video/Wildlife.mp4",
                "","LongVideoPreset");
        VodUploadClient client = initVodUploadClient();
        VodUploadResponse response = client.upload("ap-guangzhou", request);
        logger.info("Upload FileId = {}", response.getFileId());
    }

    @Test
    public void uploadTimeOut() throws Exception {
        VodUploadRequest request = new VodUploadRequest("video/Wildlife.mp4");
        VodUploadClient client = initVodUploadClient();
        VodUploadResponse response = client.upload("ap-guangzhou", request,10);
        logger.info("Upload FileId = {}", response.getFileId());
    }

    @Test
    public void ignorePreCheckSettingsToUpload() throws Exception {
        thrown.expect(TencentCloudSDKException.class);
        thrown.expectMessage("MissingParameter-The request is missing a required parameter `MediaType`.");
        VodUploadRequest request = new VodUploadRequest("video/Wildlife.mp4");
        VodUploadClient client = initVodUploadClient();
        client.setIgnorePreCheck(true);
        VodUploadResponse response = client.upload("ap-guangzhou", request);
        logger.info("Upload FileId = {}", response.getFileId());
    }

    @Test
    public void copyUtilTest() throws Exception {
        F f = new F();
        C c = CopyUtil.clone(f, C.class);
        logger.info(c.toString());
    }

    @Test
    public void FileUtilTest() throws VodClientException {
        thrown.expect(VodClientException.class);
        thrown.expectMessage("FilePath cannot be blank");
        String filePath = "video/Wildlife.mp4";
        Boolean fileExist = FileUtil.isFileExist(filePath);
        String fileName = FileUtil.getFileName(filePath);
        String fileType = FileUtil.getFileType(filePath);
        logger.info("{},{},{}",fileExist,fileName,fileType);
        filePath = "";
        fileExist = FileUtil.isFileExist(filePath);
        fileName = FileUtil.getFileName(filePath);
        fileType = FileUtil.getFileType(filePath);
        logger.info("{},{},{}",fileExist,fileName,fileType);
        filePath = null;
        fileExist = FileUtil.isFileExist(filePath);
        fileName = FileUtil.getFileName(filePath);
        fileType = FileUtil.getFileType(filePath);
        logger.info("{},{},{}",fileExist,fileName,fileType);
    }

    @Test
    public void PrintUtilTest() {
        F f = new F();
        logger.info(PrintUtil.printObject(f));
        f = null;
        logger.info(PrintUtil.printObject(f));
    }

    @Test
    public void StringUtilTest() {
        String str = "vod-java-sdk";
        boolean isBlank = StringUtil.isBlank(str);
        boolean notBlank = StringUtil.isNotBlank(str);
        boolean letterCheck = StringUtil.letterCheck(str);
        logger.info("{},{},{}",isBlank,notBlank,letterCheck);
        str = "127.0.0.1";
        letterCheck = StringUtil.letterCheck(str);
        logger.info("{}",letterCheck);
        str = null;
        isBlank = StringUtil.isBlank(str);
        notBlank = StringUtil.isNotBlank(str);
        letterCheck = StringUtil.letterCheck(str);
        logger.info("{},{},{}",isBlank,notBlank,letterCheck);
    }

    public static class F {

        private String name = "云点播SDK";
        private Integer age = 18;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }
    }

    public static class C extends F {
        private String name;
        private Integer age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }
        @Override
        public String toString() {
            return "C{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    '}';
        }
    }



}
