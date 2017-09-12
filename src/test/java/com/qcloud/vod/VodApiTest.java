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
        VodApi vodApi = new VodApi(null, null);
        vodApi.upload(null);
    }

    @Test
    public void uploadWithNullSecretKey() throws Exception {
        thrown.expect(VodParamException.class);
        thrown.expectMessage("secretKey is null");
        VodApi vodApi = new VodApi("a", null);
        vodApi.upload(null);
    }

    @Test
    public void uploadWithNullVideoPath() throws Exception {
        thrown.expect(VodParamException.class);
        thrown.expectMessage("videoPath is null");
        VodApi vodApi = new VodApi("a", "b");
        vodApi.upload(null);
    }

    @Test
    public void uploadWithInvalidVideoPath() throws Exception {
        thrown.expect(VodParamException.class);
        thrown.expectMessage("videoPath is invalid");
        VodApi vodApi = new VodApi("a", "b");
        vodApi.upload("@3112.avi");
    }

    @Test
    public void uploadWithInvalidCoverPath() throws Exception {
        thrown.expect(VodParamException.class);
        thrown.expectMessage("videoPath is invalid");
        VodApi vodApi = new VodApi("a", "b");
        vodApi.upload("@3112.avi", "@3112.png");
    }

    @Test
    public void uploadWithInvalidSecretIdAndKey() throws Exception {
        thrown.expect(VodHandleException.class);
        VodApi vodApi = new VodApi("a", "b");
        vodApi.upload("videos/Wildlife.wmv", "videos/Wildlife-cover.png");
    }

//    @Test
//    public void upload() throws Exception {
//        VodApi vodApi = new VodApi("your secretId", "your secretKey");
//        vodApi.upload("videos/Wildlife.wmv", "videos/Wildlife-cover.png", "QCVB_SimpleProcessFile(30,1,10,10)");
//    }
}
