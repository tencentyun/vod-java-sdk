package com.qcloud.vod;

import com.qcloud.vod.exception.VodClientException;
import com.qcloud.vod.model.VodUploadRequest;
import com.qcloud.vod.model.VodUploadResponse;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 点播上传测试用例
 *
 * @author jianguoxu
 */
public class VodUploadClientTest {

    private static final Logger logger = LoggerFactory.getLogger(VodUploadClientTest.class);

    @Rule
    public ExpectedException thrown= ExpectedException.none();

    public VodUploadClient initVodUploadClient() {
        VodUploadClient vodUploadClient = new VodUploadClient("your secretId", "your secretKey");
        return vodUploadClient;
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
    public void uploadMedia() throws Exception {
        VodUploadRequest request = new VodUploadRequest("video/Wildlife.mp4", "video/Wildlife-Cover.png");
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
}
