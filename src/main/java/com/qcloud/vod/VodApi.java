package com.qcloud.vod;

import com.qcloud.Module.Vod;
import com.qcloud.QcloudApiModuleCenter;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.vod.common.VodConst;
import com.qcloud.vod.common.VodCosConf;
import com.qcloud.vod.common.VodParam;
import com.qcloud.vod.exception.VodHandleException;
import com.qcloud.vod.exception.VodParamException;
import com.qcloud.vod.response.VodUploadApplyResponse;
import com.qcloud.vod.response.VodUploadCommitResponse;
import com.qcloud.vod.util.FileUtil;
import com.qcloud.vod.util.JacksonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TreeMap;

/**
 * 点播Api
 * @author jianguoxu
 * @time 2017/9/4 17:31
 */
public class VodApi {

    private static final Logger logger = LoggerFactory.getLogger(VodApi.class);

    /**
     * 检查上传入参
     * @param param
     */
    private static void checkVodUploadParam(VodParam param) {
        if (param.getSecretId() == null || param.getSecretId().length() == 0) {
            throw new VodParamException("secretId is null");
        }

        if (param.getSecretKey() == null || param.getSecretKey().length() == 0) {
            throw new VodParamException("secretKey is null");
        }

        if (param.getVideoPath() == null) {
            throw new VodParamException("videoPath is null");
        }

        if (!FileUtil.isFileExist(param.getVideoPath())) {
            throw new VodParamException("videoPath is invalid");
        }

        if (param.getCoverPath() != null && !FileUtil.isFileExist(param.getCoverPath())) {
            throw new VodParamException("coverPath is invalid");
        }
    }

    /**
     * 上传(视频+封面)
     * @param param param
     */
    public static VodUploadCommitResponse upload(VodParam param) throws Exception {
        checkVodUploadParam(param);

        TreeMap<String, Object> vodConfig = new TreeMap<String, Object>();
        vodConfig.put(VodConst.KEY_SECRET_ID, param.getSecretId());
        vodConfig.put(VodConst.KEY_SECRET_KEY, param.getSecretKey());
        vodConfig.put(VodConst.KEY_REQUEST_METHOD, "GET");

        QcloudApiModuleCenter moduleCenter = new QcloudApiModuleCenter(new Vod(), vodConfig);

        //提交上传
        VodUploadApplyResponse uploadApplyResponse = VodUpload.applyUpload(moduleCenter, param);
        String uploadApplyResponseJson = JacksonUtil.toJSon(uploadApplyResponse);
        if (uploadApplyResponse.isFail()) {
            logger.error("apply upload fail, result={}", uploadApplyResponseJson);
            throw new VodHandleException("apply upload fail", uploadApplyResponseJson);
        }
        logger.info("apply upload success, result={}", uploadApplyResponseJson);

        COSClient cosClient = VodUpload.getCosClient(param, uploadApplyResponse);
        //上传视频
        VodCosConf videoConf = new VodCosConf(
                uploadApplyResponse.getStorageBucket(),
                uploadApplyResponse.getVideo().getStoragePath(),
                param.getVideoPath()
        );
        PutObjectResult videoUploadResult = VodUpload.uploadCos(cosClient, videoConf);
        logger.info("video upload cos success, result={}", videoUploadResult);
        //上传封面
        if (param.getCoverPath() != null) {
            VodCosConf coverConf = new VodCosConf(
                    uploadApplyResponse.getStorageBucket(),
                    uploadApplyResponse.getCover().getStoragePath(),
                    param.getCoverPath()
            );
            PutObjectResult coverUploadResult = VodUpload.uploadCos(cosClient, coverConf);
            logger.info("cover upload cos success, result={}", coverUploadResult);
        }

        //确认上传
        VodUploadCommitResponse uploadCommitResponse = VodUpload.commitUpload(moduleCenter, uploadApplyResponse);
        String uploadCommitResponseJson = JacksonUtil.toJSon(uploadCommitResponse);
        if (uploadCommitResponse.isFail()) {
            logger.error("commit upload fail, result={}", uploadCommitResponseJson);
            throw new VodHandleException("commit upload fail", uploadApplyResponseJson);
        }
        logger.info("commit upload success, result={}", uploadCommitResponseJson);

        return uploadCommitResponse;
    }
}
