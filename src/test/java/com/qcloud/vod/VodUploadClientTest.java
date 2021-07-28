package com.qcloud.vod;

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

    private static final Logger logger = LoggerFactory.getLogger(VodUploadClientTest.class);

    public static String secretId = System.getenv("SECRET_ID");
    public static String secretKey = System.getenv("SECRET_KEY");

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public VodUploadClient initVodUploadClient() {
        return new VodUploadClient(secretId, secretKey);
    }

    public VodUploadClient initVodUploadClientCustomHttpProfile(HttpProfile httpProfile) {
        return new VodUploadClient(secretId, secretKey, httpProfile);
    }

    public Credentials getTemporaryCredentials() throws Exception {
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
            req.setPolicy(
                    "{\n"
                    + "\"version\": \"2.0\",\n"
                    + "\"statement\": [\n"
                    + "  {\n"
                    + "    \"effect\": \"allow\",\n"
                    + "    \"resource\": \"*\"\n"
                    + "  }\n"
                    + "]\n"
                    + "}"
            );
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
        Credentials credentials = this.getTemporaryCredentials();
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
    public void customHttpProfile() throws Exception {
        VodUploadRequest request = new VodUploadRequest("video/Wildlife.mp4");
        HttpProfile httpProfile = new HttpProfile();
        // TODO 不知如何测试
        VodUploadClient client = initVodUploadClientCustomHttpProfile(httpProfile);
        VodUploadResponse response = client.upload("ap-guangzhou", request);
        logger.info("Upload FileId = {}", response.getFileId());
    }

    @Test
    public void temporaryClient() throws Exception {
        VodUploadRequest request = new VodUploadRequest("video/Wildlife.mp4");
        request.putRequestHeader("header-name","header-value");
        VodUploadClient client = initSTSVodUploadClient();
        VodUploadResponse response = client.upload("ap-guangzhou", request,10);
        logger.info("Upload FileId = {}", response.getFileId());
    }

    @Test
    public void uploadAutoStartProcedure() throws Exception {
        VodUploadRequest request = new VodUploadRequest("video/Wildlife.mp4","","LongVideoPreset");
        VodUploadClient client = initVodUploadClient();
        VodUploadResponse response = client.upload("ap-guangzhou", request);
        logger.info("Upload FileId = {}", response.getFileId());
    }

    @Test
    public void uploadTimeOut() throws Exception {
        VodUploadRequest request = new VodUploadRequest("video/Wildlife.mp4","","LongVideoPreset");
        VodUploadClient client = initVodUploadClient();
        VodUploadResponse response = client.upload("ap-guangzhou", request,10);
        logger.info("Upload FileId = {}", response.getFileId());
    }

}
