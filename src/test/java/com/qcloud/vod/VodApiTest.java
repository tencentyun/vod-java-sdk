package com.qcloud.vod;

import com.qcloud.vod.common.VodParam;
import com.qcloud.vod.exception.VodHandleException;
import com.qcloud.vod.exception.VodParamException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author jianguoxu
 * @time 2017/9/4 19:19
 */
public class VodApiTest {

    @Rule
    public ExpectedException thrown= ExpectedException.none();

    @Test
    public void uploadWithNullSecretId() throws Exception {
        thrown.expect(VodParamException.class);
        thrown.expectMessage("secretId is null");
        VodParam param = new VodParam(null, null,null, null, null);
        VodApi.upload(param);
    }

    @Test
    public void uploadWithNullSecretKey() throws Exception {
        thrown.expect(VodParamException.class);
        thrown.expectMessage("secretKey is null");
        VodParam param = new VodParam("a", null,null, null, null);
        VodApi.upload(param);
    }

    @Test
    public void uploadWithNullVideoPath() throws Exception {
        thrown.expect(VodParamException.class);
        thrown.expectMessage("videoPath is null");
        VodParam param = new VodParam("a", "b",null, null, null);
        VodApi.upload(param);
    }

    @Test
    public void uploadWithInvalidVideoPath() throws Exception {
        thrown.expect(VodParamException.class);
        thrown.expectMessage("videoPath is invalid");
        VodParam param = new VodParam("a", "b","@3112.avi", null, null);
        VodApi.upload(param);
    }

    @Test
    public void uploadWithInvalidCoverPath() throws Exception {
        thrown.expect(VodParamException.class);
        thrown.expectMessage("videoPath is invalid");
        VodParam param = new VodParam("a", "b","@3112.avi", "@3112.png", null);
        VodApi.upload(param);
    }

    @Test
    public void uploadWithInvalidSecretIdAndKey() throws Exception {
        thrown.expect(VodHandleException.class);
        VodParam param = new VodParam("a", "b", "videos/Wildlife.wmv", "videos/Wildlife-cover.png", null);
        VodApi.upload(param);
    }

    @Test
    public void upload() throws Exception {
        VodParam param = new VodParam(
                "your secretId",
                "your secretKey",
                "videos/Wildlife.wmv",
                "videos/Wildlife-cover.png",
                "QCVB_SimpleProcessFile(30,1,10,10)"
        );
        VodApi.upload(param);
    }
}
