package com.qcloud.vod;

import com.qcloud.QcloudApiModuleCenter;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;
import com.qcloud.vod.common.VodConst;
import com.qcloud.vod.common.VodCosConf;
import com.qcloud.vod.common.VodParam;
import com.qcloud.vod.exception.VodParamException;
import com.qcloud.vod.response.VodUploadApplyResponse;
import com.qcloud.vod.response.VodUploadCommitResponse;
import com.qcloud.vod.util.FileUtil;
import com.qcloud.vod.util.JacksonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.TreeMap;

/**
 * 点播上传
 * @author jianguoxu
 * @time 2017/9/4 17:36
 */
public class VodUpload {

    private static final Logger logger = LoggerFactory.getLogger(VodUpload.class);

    /**
     * 提交上传
     * @param moduleCenter moduleCenter
     * @param vodParam param
     * @return
     * @throws Exception
     */
    public static VodUploadApplyResponse applyUpload(QcloudApiModuleCenter moduleCenter, VodParam vodParam) throws Exception {
        TreeMap<String, Object> params = new TreeMap<String, Object>();

        if (vodParam.getVideoPath() == null) {
            throw new VodParamException("videoPath is null");
        }

        params.put(VodConst.KEY_VIDEO_NAME, FileUtil.getFileName(vodParam.getVideoPath()));
        params.put(VodConst.KEY_VIDEO_SIZE, FileUtil.getFileSize(vodParam.getVideoPath()));
        params.put(VodConst.KEY_VIDEO_TYPE, FileUtil.getFileType(vodParam.getVideoPath()));

        if (vodParam.getCoverPath() != null) {
            params.put(VodConst.KEY_COVER_NAME, FileUtil.getFileName(vodParam.getCoverPath()));
            params.put(VodConst.KEY_COVER_SIZE, FileUtil.getFileSize(vodParam.getCoverPath()));
            params.put(VodConst.KEY_COVER_TYPE, FileUtil.getFileType(vodParam.getCoverPath()));
        }

        if (vodParam.getProcedure() != null) {
            params.put(VodConst.KEY_PROCEDURE, vodParam.getProcedure());
        }

        String result = null;
        try {
            result = moduleCenter.call(VodConst.MODULE_APPLY_UPLOAD, params);
            VodUploadApplyResponse response = JacksonUtil.readValue(result, VodUploadApplyResponse.class);
            return response;
        } catch (Exception e) {
            logger.error("apply upload error, param=" + vodParam, e);
            throw e;
        }
    }

    /**
     * 获取CosClient
     * @param param
     * @param uploadApplyResponse
     * @return
     */
    public static COSClient getCosClient(VodParam param, VodUploadApplyResponse uploadApplyResponse) {
        String region = "cos." + uploadApplyResponse.getStorageRegionV5();
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        clientConfig.setSignExpired(24 * 3600);

        COSCredentials credentials = new BasicCOSCredentials(String.valueOf(uploadApplyResponse.getStorageAppId()), param.getSecretId(), param.getSecretKey());
        COSClient cosClient = new COSClient(credentials, clientConfig);

        return cosClient;
    }

    /**
     * 上传文件到Cos
     * @param cosClient
     * @param conf
     * @return
     */
    public static PutObjectResult uploadCos(COSClient cosClient, VodCosConf conf) {
        File file = new File(conf.getFilePath());
        PutObjectRequest coverPutRequest = new PutObjectRequest(conf.getBucketName(), conf.getStoragePath(), file);

        try {
            PutObjectResult result = cosClient.putObject(coverPutRequest);
            return result;
        } catch (Exception e) {
            logger.error("upload cos error, conf=" + conf, e);
            throw e;
        }
    }

    /**
     * 确认上传
     * @param moduleCenter
     * @param uploadApplyResponse
     * @return
     * @throws Exception
     */
    public static VodUploadCommitResponse commitUpload(QcloudApiModuleCenter moduleCenter, VodUploadApplyResponse uploadApplyResponse) throws Exception {
        TreeMap<String, Object> params = new TreeMap<String, Object>();

        params.put(VodConst.KEY_SESSION_KEY, uploadApplyResponse.getVodSessionKey());

        String result = null;
        try {
            result = moduleCenter.call(VodConst.MODULE_COMMIT_UPLOAD, params);
            VodUploadCommitResponse response = JacksonUtil.readValue(result, VodUploadCommitResponse.class);
            return response;
        } catch (Exception e) {
            logger.error("commit upload error, vodSessionKey=" + uploadApplyResponse.getVodSessionKey(), e);
            throw e;
        }
    }
}
