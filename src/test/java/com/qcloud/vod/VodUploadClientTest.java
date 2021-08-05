package com.qcloud.vod;

import com.qcloud.cos.internal.Constants;
import com.qcloud.vod.common.FileUtil;
import com.qcloud.vod.common.UrlUtil;
import com.qcloud.vod.exception.VodClientException;
import com.qcloud.vod.model.VodUploadRequest;
import com.qcloud.vod.model.VodUploadResponse;
import com.qcloud.vod.model.VodUrlUploadRequest;
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
        Credential cred = new Credential(secretId, secretKey);
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint("sts.tencentcloudapi.com");
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);
        StsClient client = new StsClient(cred, "ap-chengdu", clientProfile);
        GetFederationTokenRequest req = new GetFederationTokenRequest();
        req.setName("customName");
        req.setPolicy(
                "{\"version\": \"2.0\",\"statement\": [{\"effect\": \"allow\",\"resource\": \"*\"}]}");
        req.setDurationSeconds(1800);
        GetFederationTokenResponse resp = client.GetFederationToken(req);
        return resp.getCredentials();
    }

    public VodUploadClient initSTSVodUploadClient() throws Exception {
        Credentials credentials = this.obtainTemporaryCredentials();
        return new VodUploadClient(
                credentials.getTmpSecretId(), credentials.getTmpSecretKey(), credentials.getToken());
    }

    @Test
    public void lackMediaPath() throws Exception {
        this.thrown.expect(VodClientException.class);
        this.thrown.expectMessage("lack media path");
        VodUploadRequest request = new VodUploadRequest();
        VodUploadClient client = this.initVodUploadClient();
        client.upload("ap-guangzhou", request);
    }

    @Test
    public void lackMediaType() throws Exception {
        this.thrown.expect(VodClientException.class);
        this.thrown.expectMessage("lack media type");
        VodUploadRequest request = new VodUploadRequest();
        request.setMediaFilePath("video/Wildlife");
        VodUploadClient client = this.initVodUploadClient();
        client.upload("ap-guangzhou", request);
    }

    @Test
    public void invalidMediaPath() throws Exception {
        this.thrown.expect(VodClientException.class);
        this.thrown.expectMessage("media path is invalid");
        VodUploadRequest request = new VodUploadRequest();
        request.setMediaFilePath("video/WildlifeA");
        VodUploadClient client = this.initVodUploadClient();
        client.upload("ap-guangzhou", request);
    }

    @Test
    public void invalidCoverPath() throws Exception {
        this.thrown.expect(VodClientException.class);
        this.thrown.expectMessage("cover path is invalid");
        VodUploadRequest request = new VodUploadRequest();
        request.setMediaFilePath("video/Wildlife.mp4");
        request.setCoverFilePath("video/Wildlife-CoverA");
        VodUploadClient client = this.initVodUploadClient();
        client.upload("ap-guangzhou", request);
    }

    @Test
    public void lackCoverType() throws Exception {
        this.thrown.expect(VodClientException.class);
        this.thrown.expectMessage("lack cover type");
        VodUploadRequest request = new VodUploadRequest();
        request.setMediaFilePath("video/Wildlife.mp4");
        request.setCoverFilePath("video/Wildlife-Cover");
        VodUploadClient client = this.initVodUploadClient();
        client.upload("ap-guangzhou", request);
    }

    @Test
    public void invalidMediaType() throws Exception {
        this.thrown.expect(TencentCloudSDKException.class);
        this.thrown.expectMessage("InvalidParameterValue.MediaType-invalid media type");
        VodUploadRequest request = new VodUploadRequest();
        request.setMediaFilePath("video/Wildlife.mp4");
        request.setMediaType("test");
        VodUploadClient client = this.initVodUploadClient();
        client.upload("ap-guangzhou", request);
    }

    @Test
    public void invalidCoverType() throws Exception {
        this.thrown.expect(TencentCloudSDKException.class);
        this.thrown.expectMessage("InvalidParameterValue.CoverType-invalid cover type");
        VodUploadRequest request = new VodUploadRequest();
        request.setMediaFilePath("video/Wildlife.mp4");
        request.setCoverFilePath("video/Wildlife-Cover.png");
        request.setCoverType("test");
        VodUploadClient client = this.initVodUploadClient();
        client.upload("ap-guangzhou", request);
    }

    @Test
    public void invalidNoFileName() throws Exception {
        VodUploadRequest request = new VodUploadRequest();
        request.setMediaFilePath("video/.mp4");
        VodUploadClient client = this.initVodUploadClient();
        client.upload("ap-guangzhou", request);
    }

    @Test
    public void uploadMedia() throws Exception {
        VodUploadRequest request =
                new VodUploadRequest("video/Wildlife.mp4", "video/Wildlife-Cover.png");
        request.setStorageRegion("ap-chongqing");
        request.setMediaName("test-20181129-1423");
        VodUploadClient client = this.initVodUploadClient();
        VodUploadResponse response = client.upload("ap-guangzhou", request);
        logger.info("Upload FileId = {}", response.getFileId());
    }

    @Test
    public void uploadHls() throws Exception {
        VodUploadRequest request = new VodUploadRequest("video/hls/prog_index.m3u8", "");
        VodUploadClient client = this.initVodUploadClient();
        VodUploadResponse response = client.upload("ap-guangzhou", request);
        logger.info("Upload FileId = {}", response.getFileId());
    }

    @Test
    public void uploadMasterPlaylist() throws Exception {
        VodUploadRequest request = new VodUploadRequest("video/hls/bipbopall.m3u8", "");
        VodUploadClient client = this.initVodUploadClient();
        VodUploadResponse response = client.upload("ap-guangzhou", request);
        logger.info("Upload FileId = {}", response.getFileId());
    }

    @Test
    public void uploadWithDisableSecurityCos() throws Exception {
        VodUploadRequest request = new VodUploadRequest("video/Wildlife.mp4");
        request.disableSecureUpload();
        VodUploadClient client = this.initVodUploadClient();
        VodUploadResponse response = client.upload("ap-guangzhou", request);
        logger.info("Upload FileId = {}", response.getFileId());
    }

    @Test
    public void uploadWithConcurrentUploadNumber() throws Exception {
        VodUploadRequest request = new VodUploadRequest("video/Wildlife.mp4");
        request.setConcurrentUploadNumber(10);
        VodUploadClient client = this.initVodUploadClient();
        VodUploadResponse response = client.upload("ap-guangzhou", request);
        logger.info("Upload FileId = {}", response.getFileId());
    }

    @Test
    public void uploadByCustomHeader() throws Exception {
        VodUploadRequest request = new VodUploadRequest("video/Wildlife.mp4");
        request.putRequestHeader("header-name", "header-value");
        VodUploadClient client = this.initVodUploadClient();
        VodUploadResponse response = client.upload("ap-guangzhou", request);
        logger.info("Upload FileId = {}", response.getFileId());
    }

    @Test
    public void deferProxyPort() throws Exception {
        this.thrown.expect(TencentCloudSDKException.class);
        this.thrown.expectMessage("java.net.UnknownHostException-@.noHostUrl.noHostUrl");
        VodUploadRequest request = new VodUploadRequest("video/Wildlife.mp4");
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setProxyHost("@.noHostUrl.noHostUrl");
        VodUploadClient client = this.initVodUploadClientCustomHttpProfile(httpProfile);
        VodUploadResponse response = client.upload("ap-guangzhou", request);
        logger.info("Upload FileId = {}", response.getFileId());
    }

    @Test
    public void customHttpProfileUpload() throws Exception {
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setProtocol("http://");
        VodUploadClient client = this.initVodUploadClientCustomHttpProfile(httpProfile);
        client.setRetryCount(10);
        VodUploadRequest request = new VodUploadRequest("video/Wildlife.mp4");
        VodUploadResponse response = client.upload("ap-guangzhou", request);
        logger.info("Upload FileId = {}", response.getFileId());
    }

    @Test
    public void customRetryCount() throws Exception {
        VodUploadRequest request = new VodUploadRequest("video/Wildlife.mp4");
        VodUploadClient client = this.initVodUploadClient();
        client.setRetryCount(1);
        VodUploadResponse response = client.upload("ap-guangzhou", request);
        logger.info("Upload FileId = {}", response.getFileId());
    }

    @Test
    public void subApplication() throws Exception {
        this.thrown.expect(TencentCloudSDKException.class);
        this.thrown.expectMessage("InvalidParameterValue.SubAppId-invalid subappid");
        VodUploadRequest request = new VodUploadRequest("video/Wildlife.mp4");
        request.setSubAppId(13008543630L);
        VodUploadClient client = this.initVodUploadClient();
        VodUploadResponse response = client.upload("ap-guangzhou", request);
        logger.info("Upload FileId = {}", response.getFileId());
    }

    @Test
    public void temporarySTSClient() throws Exception {
        VodUploadRequest request = new VodUploadRequest("video/Wildlife.mp4");
        request.putRequestHeader("header-name", "header-value");
        VodUploadClient client = this.initSTSVodUploadClient();
        VodUploadResponse response = client.upload("ap-guangzhou", request);
        logger.info("Upload FileId = {}", response.getFileId());
    }

    @Test
    public void uploadAutoStartProcedure() throws Exception {
        VodUploadRequest request =
                new VodUploadRequest("video/Wildlife.mp4");
        request.setProcedure("LongVideoPreset");
        VodUploadClient client = this.initVodUploadClient();
        VodUploadResponse response = client.upload("ap-guangzhou", request);
        logger.info("Upload FileId = {}", response.getFileId());
    }

    @Test
    public void uploadTimeOut() throws Exception {
        VodUploadRequest request = new VodUploadRequest("video/Wildlife.mp4");
        VodUploadClient client = this.initVodUploadClient();
        VodUploadResponse response = client.upload("ap-guangzhou", request, 10);
        logger.info("Upload FileId = {}", response.getFileId());
    }

    @Test
    public void ignorePreCheckSettingsToUpload() throws Exception {
        this.thrown.expect(TencentCloudSDKException.class);
        this.thrown.expectMessage(
                "MissingParameter-The request is missing a required parameter `MediaType`.");
        VodUploadRequest request = new VodUploadRequest("video/Wildlife.mp4");
        VodUploadClient client = this.initVodUploadClient();
        client.setIgnorePreCheck(true);
        VodUploadResponse response = client.upload("ap-guangzhou", request);
        logger.info("Upload FileId = {}", response.getFileId());
    }

    @Test
    public void fileUtilTest() {
        String filePath = "video/Wildlife.mp4";
        Boolean fileExist = FileUtil.isFileExist(filePath);
        String fileName = FileUtil.getFileName(filePath);
        String fileType = FileUtil.getFileType(filePath);
        logger.info("{},{},{}", fileExist, fileName, fileType);
    }

    @Test
    public void uploadBigFile() throws Exception {
        VodUploadRequest request = new VodUploadRequest("video/bigFile.mp4");
        VodUploadClient client = this.initVodUploadClient();
        VodUploadResponse response = client.upload("ap-guangzhou", request);
        logger.info("Upload FileId = {}", response.getFileId());
    }

    @Test
    public void customSliceSettings() throws Exception {
        VodUploadRequest request = new VodUploadRequest("video/bigFile.mp4");
        request.setConcurrentUploadNumber(3);
        request.setMinimumUploadPartSize(10 * Constants.MB);
        request.setMultipartUploadThreshold(10 * Constants.MB);
        VodUploadClient client = this.initVodUploadClient();
        VodUploadResponse response = client.upload("ap-guangzhou", request);
        logger.info("Upload FileId = {}", response.getFileId());
    }

    @Test
    public void urlUtilTest() throws Exception {
        String mediaUrl =
                "http://1300854363.vod2.myqcloud.com/96a48d63vodcq1300854363/e40970823701925920154859610/5Gka9KfAi3MA.mp4";
        String fileName = UrlUtil.getUrlFileName(mediaUrl);
        String fileType = UrlUtil.getUrlFileType(mediaUrl);
        logger.info("fileName:{},fileType:{}", fileName, fileType);
        mediaUrl = "/5Gka9KfAi3MA.mp4";
        fileName = UrlUtil.getUrlFileName(mediaUrl);
        fileType = UrlUtil.getUrlFileType(mediaUrl);
        logger.info("fileName:{},fileType:{}", fileName, fileType);
        mediaUrl = "5Gka9KfAi3MA.mp4";
        fileName = UrlUtil.getUrlFileName(mediaUrl);
        fileType = UrlUtil.getUrlFileType(mediaUrl);
        logger.info("fileName:{},fileType:{}", fileName, fileType);
    }

    @Test
    public void uploadFromUrl1() throws Exception {
        String mediaUrl =
                "http://1300854363.vod2.myqcloud.com/96a48d63vodcq1300854363/e40970823701925920154859610/5Gka9KfAi3MA.mp4";
        VodUploadRequest request = new VodUrlUploadRequest(mediaUrl);
        request.setConcurrentUploadNumber(3);
        request.setMinimumUploadPartSize(10 * Constants.MB);
        request.setMultipartUploadThreshold(10 * Constants.MB);
        VodUploadClient client = this.initVodUploadClient();
        VodUploadResponse response = client.upload("ap-guangzhou", request);
        logger.info("Upload FileId = {}", response.getFileId());
    }

    @Test
    public void uploadFromUrl2() throws Exception {
        String mediaUrl =
                "http://1300854363.vod2.myqcloud.com/96a48d63vodcq1300854363/e40970823701925920154859610/5Gka9KfAi3MA.mp4";
        String coverFilePath = "video/Wildlife-cover.png";
        VodUploadRequest request = new VodUrlUploadRequest(mediaUrl);
        request.setCoverFilePath(coverFilePath);
        request.setConcurrentUploadNumber(3);
        request.setMinimumUploadPartSize(10 * Constants.MB);
        request.setMultipartUploadThreshold(10 * Constants.MB);
        VodUploadClient client = this.initVodUploadClient();
        VodUploadResponse response = client.upload("ap-guangzhou", request);
        logger.info("Upload FileId = {}", response.getFileId());
    }

    @Test
    public void uploadFromUrl3() throws Exception {
        String mediaFilePath = "video/Wildlife.mp4";
        String coverUrl =
                "http://1300854363.vod2.myqcloud.com/96a48d63vodcq1300854363/8cfc31023701925921407776151/3701925921407776152.png";
        VodUploadRequest request = new VodUrlUploadRequest("", coverUrl);
        request.setMediaFilePath(mediaFilePath);
        request.setConcurrentUploadNumber(3);
        request.setMinimumUploadPartSize(10 * Constants.MB);
        request.setMultipartUploadThreshold(10 * Constants.MB);
        VodUploadClient client = this.initVodUploadClient();
        VodUploadResponse response = client.upload("ap-guangzhou", request);
        logger.info("Upload FileId = {}", response.getFileId());
    }

    @Test
    public void uploadFromUrl4() throws Exception {
        String mediaUrl =
                "http://1300854363.vod2.myqcloud.com/96a48d63vodcq1300854363/e40970823701925920154859610/5Gka9KfAi3MA.mp4";
        String coverUrl =
                "http://1300854363.vod2.myqcloud.com/96a48d63vodcq1300854363/8cfc31023701925921407776151/3701925921407776152.png";
        VodUploadRequest request = new VodUrlUploadRequest(mediaUrl, coverUrl);
        request.setConcurrentUploadNumber(3);
        request.setMinimumUploadPartSize(10 * Constants.MB);
        request.setMultipartUploadThreshold(10 * Constants.MB);
        VodUploadClient client = this.initVodUploadClient();
        VodUploadResponse response = client.upload("ap-guangzhou", request);
        logger.info("Upload FileId = {}", response.getFileId());
    }

}
