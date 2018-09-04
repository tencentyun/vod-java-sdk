package com.qcloud.vod;

import com.qcloud.Module.Vod;
import com.qcloud.QcloudApiModuleCenter;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.transfer.TransferManager;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * 点播Api
 * @author jianguoxu
 * @time 2017/9/4 17:31
 */
public class VodApi {

    private static final Logger logger = LoggerFactory.getLogger(VodApi.class);

    //secretId
    private String secretId;

    //secretKey
    private String secretKey;

    //签名有效时长(秒级)
    private int signExpired;

    //并发上传数目
    private int concurrentUploadNumber;

    public VodApi(String secretId, String secretKey) {
        //设置默认的签名有效时长
        this(secretId, secretKey, 24 * 3600, 1);
    }

    public VodApi(String secretId, String secretKey, int concurrentUploadNumber) {
        this(secretId, secretKey, 24 * 3600, concurrentUploadNumber);
    }

    public VodApi(String secretId, String secretKey, int signExpired, int concurrentUploadNumber) {
        this.secretId = secretId;
        this.secretKey = secretKey;
        this.signExpired = signExpired;
        this.concurrentUploadNumber = concurrentUploadNumber;
    }

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
     * 上传(视频）
     * @param videoPath
     * @return
     * @throws Exception
     */
    public VodUploadCommitResponse upload(String videoPath) throws Exception {
        return upload(videoPath, null);
    }

    /**
     * 上传(视频+封面)
     * @param videoPath
     * @param coverPath
     * @return
     * @throws Exception
     */
    public VodUploadCommitResponse upload(String videoPath, String coverPath) throws Exception {
        return upload(videoPath, coverPath, Collections.EMPTY_MAP);
    }

    /**
     * 上传(视频+封面+任务流)
     * @param videoPath
     * @param coverPath
     * @param procedure
     * @return
     * @throws Exception
     */
    public VodUploadCommitResponse upload(String videoPath, String coverPath, String procedure) throws Exception {
        Map<String, Object> extraParams = new HashMap<String, Object>();
        extraParams.put(VodConst.KEY_PROCEDURE, procedure);
        return upload(videoPath, coverPath, extraParams);
    }

    public VodUploadCommitResponse upload(String videoPath, String coverPath, Map<String, Object> extraParams) throws Exception {
        VodParam param = new VodParam();
        param.setSecretId(secretId);
        param.setSecretKey(secretKey);
        param.setVideoPath(videoPath);
        param.setCoverPath(coverPath);
        checkVodUploadParam(param);

        TreeMap<String, Object> vodConfig = new TreeMap<String, Object>();
        vodConfig.put(VodConst.KEY_SECRET_ID, param.getSecretId());
        vodConfig.put(VodConst.KEY_SECRET_KEY, param.getSecretKey());
        vodConfig.put(VodConst.KEY_REQUEST_METHOD, "GET");

        QcloudApiModuleCenter moduleCenter = new QcloudApiModuleCenter(new Vod(), vodConfig);

        //提交上传
        VodUploadApplyResponse uploadApplyResponse = VodUpload.applyUpload(moduleCenter, param, extraParams);
        String uploadApplyResponseJson = JacksonUtil.toJSon(uploadApplyResponse);
        if (uploadApplyResponse.isFail()) {
            logger.error("apply upload fail, result={}", uploadApplyResponseJson);
            throw new VodHandleException("apply upload fail", uploadApplyResponseJson);
        }
        logger.info("apply upload success, result={}", uploadApplyResponseJson);

        TransferManager transferManager = VodUpload.getTransferManager(param, uploadApplyResponse, signExpired, concurrentUploadNumber);
        try {
            //上传视频
            VodCosConf videoConf = new VodCosConf(
                    uploadApplyResponse.getStorageBucket() + "-" + uploadApplyResponse.getStorageAppId(),
                    uploadApplyResponse.getVideo().getStoragePath(),
                    param.getVideoPath()
            );
            VodUpload.uploadCos(transferManager, videoConf);
            logger.info("video upload cos success");
            //上传封面
            if (param.getCoverPath() != null) {
                VodCosConf coverConf = new VodCosConf(
                        uploadApplyResponse.getStorageBucket() + "-" + uploadApplyResponse.getStorageAppId(),
                        uploadApplyResponse.getCover().getStoragePath(),
                        param.getCoverPath()
                );
                VodUpload.uploadCos(transferManager, coverConf);
                logger.info("cover upload cos success");
            }
        } catch (Exception e) {
            logger.error("upload cos fail", e);
            throw e;
        } finally {
            transferManager.shutdownNow();
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
