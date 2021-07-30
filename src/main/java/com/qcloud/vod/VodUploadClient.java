package com.qcloud.vod;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.BasicSessionCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.transfer.TransferManager;
import com.qcloud.cos.transfer.Upload;
import com.qcloud.vod.common.CopyUtil;
import com.qcloud.vod.common.FileUtil;
import com.qcloud.vod.common.PrintUtil;
import com.qcloud.vod.common.RegularUtil;
import com.qcloud.vod.common.StringUtil;
import com.qcloud.vod.exception.VodClientException;
import com.qcloud.vod.model.VodUploadRequest;
import com.qcloud.vod.model.VodUploadResponse;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.vod.v20180717.VodClient;
import com.tencentcloudapi.vod.v20180717.models.ApplyUploadRequest;
import com.tencentcloudapi.vod.v20180717.models.ApplyUploadResponse;
import com.tencentcloudapi.vod.v20180717.models.CommitUploadRequest;
import com.tencentcloudapi.vod.v20180717.models.CommitUploadResponse;
import com.tencentcloudapi.vod.v20180717.models.ParseStreamingManifestRequest;
import com.tencentcloudapi.vod.v20180717.models.ParseStreamingManifestResponse;
import com.tencentcloudapi.vod.v20180717.models.TempCertificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * VOD upload client
 *
 * @author jianguoxu
 */
public class VodUploadClient {

    private static final Logger logger = LoggerFactory.getLogger(VodUploadClient.class);

    private final String secretId;

    private final String secretKey;

    private final String token;

    private HttpProfile httpProfile;

    private Integer retryCount = 3;

    private boolean ignorePreCheck;

    public VodUploadClient(String secretId, String secretKey) {
        this(secretId, secretKey, "");
    }

    public VodUploadClient(String secretId, String secretKey, HttpProfile httpProfile) {
        this(secretId, secretKey, "");
        this.httpProfile = httpProfile;
    }

    public VodUploadClient(String secretId, String secretKey, String token) {
        if (StringUtil.isBlank(secretId) || StringUtil.isBlank(secretKey)) {
            throw new CosClientException(
                    "secretId or secretKey is blank, you must set legal key pair when init vodUploadClient.");
        }
        this.secretId = secretId;
        this.secretKey = secretKey;
        this.token = token;
    }

    /**
     * Master upload
     */
    public VodUploadResponse upload(String region, VodUploadRequest request)
            throws Exception {
        this.beforeUploadCheck(region,request);

        Credential credential = new Credential(secretId, secretKey, token);
        VodClient vodClient;
        if (needHttpProxy()) {
            this.defaultDomainProxyPort();
            vodClient = new VodClient(credential, region, new ClientProfile(ClientProfile.SIGN_TC3_256, httpProfile));
        } else {
            vodClient = new VodClient(credential, region);
        }

        // Three steps to upload
        ApplyUploadResponse applyUploadResponse = this.applicationUpload(request, vodClient);
        this.uploadCos(request, vodClient, applyUploadResponse);
        CommitUploadResponse commitUploadResponse = this.commitUpload(request, applyUploadResponse, vodClient);

        VodUploadResponse uploadResponse;
        try {
            uploadResponse = CopyUtil.clone(commitUploadResponse, VodUploadResponse.class);
        } catch (Exception e) {
            throw new VodClientException(e);
        }

        return uploadResponse;
    }

    /**
     * Upload by time limited (Encapsulation master upload)
     */
    public VodUploadResponse upload(final String region, final VodUploadRequest request,
                                    int timeoutSeconds) throws Exception {
        final VodUploadClient vodUploadClient = this;

        Callable<VodUploadResponse> task = new Callable<VodUploadResponse>() {
            public VodUploadResponse call() throws Exception {
                return vodUploadClient.upload(region, request);
            }
        };

        ExecutorService service = Executors.newSingleThreadExecutor();
        Future<VodUploadResponse> future = service.submit(task);
        try {
            return future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new VodClientException(e);
        } finally {
            service.shutdown();
        }
    }

    /**
     * BeforeUploadCheck
     */
    private void beforeUploadCheck(String region, VodUploadRequest request) throws VodClientException {
        if (!ignorePreCheck) {
            prefixCheckAndSetDefaultVal(region, request);
        }
    }

    /**
     * Step 1: Application upload
     */
    private ApplyUploadResponse applicationUpload(VodUploadRequest request, VodClient vodClient) throws Exception {
        ApplyUploadRequest applyUploadRequest = ApplyUploadRequest
                .fromJsonString(VodUploadRequest.toJsonString(request), ApplyUploadRequest.class);
        ApplyUploadResponse applyUploadResponse = applyUploadDo(vodClient, applyUploadRequest);
        logger.info("ApplyUpload Response = {}", PrintUtil.printObject(applyUploadResponse));
        return applyUploadResponse;
    }

    /**
     * Step 2: upload file to cos
     */
    private void uploadCos(VodUploadRequest request, VodClient vodClient,
                           ApplyUploadResponse applyUploadResponse) throws Exception {

        COSClient cosClient = this.createCosClient(request,applyUploadResponse);

        TransferManager transferManager;
        if (request.getConcurrentUploadNumber() != null && request.getConcurrentUploadNumber() > 0) {
            ExecutorService fixedThreadPool =
                    Executors.newFixedThreadPool(request.getConcurrentUploadNumber());
            transferManager = new TransferManager(cosClient, fixedThreadPool);
        } else {
            transferManager = new TransferManager(cosClient);
        }

        if (StringUtil.isNotBlank(request.getMediaType())
                && StringUtil.isNotBlank(applyUploadResponse.getMediaStoragePath())) {
            uploadCosDo(transferManager, request.getMediaFilePath(), applyUploadResponse.getStorageBucket(),
                    applyUploadResponse.getMediaStoragePath(),request.getRequestHeader());
        }
        if (StringUtil.isNotBlank(request.getCoverType())
                && StringUtil.isNotBlank(applyUploadResponse.getCoverStoragePath())) {
            uploadCosDo(transferManager, request.getCoverFilePath(), applyUploadResponse.getStorageBucket(),
                    applyUploadResponse.getCoverStoragePath(),request.getRequestHeader());
        }

        this.updateSegmentFile(vodClient,request,applyUploadResponse,transferManager);

        transferManager.shutdownNow();
    }

    /**
     * Step 3: commitUpload
     */
    private CommitUploadResponse commitUpload(VodUploadRequest request, ApplyUploadResponse applyUploadResponse,
                                              VodClient vodClient) throws Exception {
        CommitUploadRequest commitUploadRequest = new CommitUploadRequest();
        commitUploadRequest.setVodSessionKey(applyUploadResponse.getVodSessionKey());
        commitUploadRequest.setSubAppId(request.getSubAppId());
        CommitUploadResponse commitUploadResponse = commitUploadDo(vodClient, commitUploadRequest);
        logger.info("CommitUpload Response = {}", PrintUtil.printObject(commitUploadResponse));
        return commitUploadResponse;
    }

    /**
     * Creat CosClient
     */
    private COSClient createCosClient(VodUploadRequest request, ApplyUploadResponse applyUploadResponse) {
        COSCredentials credentials;
        if (applyUploadResponse.getTempCertificate() != null) {
            TempCertificate certificate = applyUploadResponse.getTempCertificate();
            credentials = new BasicSessionCredentials(certificate.getSecretId(),
                    certificate.getSecretKey(), certificate.getToken());
        } else {
            credentials = new BasicCOSCredentials(secretId, secretKey);
        }
        ClientConfig clientConfig = new ClientConfig(new Region(applyUploadResponse.getStorageRegion()));
        if (request.getSecureUpload()) {
            clientConfig.setHttpProtocol(HttpProtocol.https);
        }
        if (needHttpProxy()) {
            clientConfig.setHttpProxyIp(httpProfile.getProxyHost());
            clientConfig.setHttpProxyPort(httpProfile.getProxyPort());
            if (httpProfile.getProxyUsername() != null) {
                clientConfig.setProxyUsername(httpProfile.getProxyUsername());
                clientConfig.setProxyPassword(httpProfile.getProxyPassword());
                clientConfig.setUseBasicAuth(true);
            }
        }
        return new COSClient(credentials, clientConfig);
    }

    /**
     * Apply for upload
     */
    private ApplyUploadResponse applyUploadDo(VodClient client, ApplyUploadRequest request) throws Exception {
        TencentCloudSDKException err = new TencentCloudSDKException("apply for upload fail");
        for (int i = 0; i < retryCount; i++) {
            try {
                return client.ApplyUpload(request);
            } catch (TencentCloudSDKException exception) {
                if (StringUtil.isBlank(exception.getRequestId())) {
                    err = exception;
                    continue;
                }
                throw exception;
            }
        }
        throw err;
    }

    /**
     * Upload segment file
     */
    private void updateSegmentFile(VodClient vodClient, VodUploadRequest request,
                                   ApplyUploadResponse applyUploadResponse,
                                   TransferManager transferManager) throws Exception {
        List<String> segmentUrlList = new ArrayList<>();
        if (isManifestMediaType(request.getMediaType())) {
            Set<String> parsedManifestSet = new HashSet<>();
            parseManifest(vodClient, request.getMediaFilePath(), request.getMediaType(), parsedManifestSet,
                    segmentUrlList);
        }
        for (String segmentUrl : segmentUrlList) {
            String cosDir =
                    Paths.get(applyUploadResponse.getMediaStoragePath()).getParent().toString();
            String parentPath =
                    Paths.get(request.getMediaFilePath()).getParent().toString();
            String segmentPath = segmentUrl.substring(parentPath.length());
            String segmentStoragePath =
                    Paths.get(cosDir, segmentPath).toString().replace('\\', '/');
            String segmentFilePath =
                    Paths.get(segmentUrl).toString().replace('\\', '/');

            uploadCosDo(transferManager, segmentFilePath,
                    applyUploadResponse.getStorageBucket(), segmentStoragePath.substring(1),
                    request.getRequestHeader());
        }
    }

    /**
     * COS upload
     */
    private void uploadCosDo(TransferManager transferManager, String localPath, String bucket, String cosPath,
                             Map<String,String> headersMap) throws Exception {
        File file = new File(localPath);
        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket,cosPath,file);
            if (headersMap != null) {
                for (Map.Entry<String, String> entry : headersMap.entrySet()) {
                    putObjectRequest.putCustomRequestHeader(entry.getKey(),entry.getValue());
                }
            }
            Upload upload = transferManager.upload(putObjectRequest);
            upload.waitForCompletion();
        } catch (Exception e) {
            logger.error("Upload Cos Err", e);
            throw e;
        }
    }

    /**
     * Confirm upload
     */
    private CommitUploadResponse commitUploadDo(VodClient client, CommitUploadRequest request) throws Exception {
        TencentCloudSDKException err = new TencentCloudSDKException("confirm upload fail");
        for (int i = 0; i < retryCount; i++) {
            try {
                return client.CommitUpload(request);
            } catch (TencentCloudSDKException e) {
                if (StringUtil.isBlank(e.getRequestId())) {
                    err = e;
                    continue;
                }
                throw e;
            }
        }
        throw err;
    }

    /**
     * Parse index file on server to get segment information
     */
    private ParseStreamingManifestResponse parseStreamingManifest(
            VodClient client, ParseStreamingManifestRequest request) throws Exception {
        TencentCloudSDKException err =
                new TencentCloudSDKException("parse index file on server to get segment information fail");
        for (int i = 0; i < retryCount; i++) {
            try {
                return client.ParseStreamingManifest(request);
            } catch (TencentCloudSDKException e) {
                if (StringUtil.isBlank(e.getRequestId())) {
                    err = e;
                    continue;
                }
                throw e;
            }
        }
        throw err;
    }

    /**
     * Pre-check and set default values
     */
    private void prefixCheckAndSetDefaultVal(String region, VodUploadRequest request) throws VodClientException {
        if (StringUtil.isBlank(region)) {
            throw new VodClientException("lack region");
        }
        this.mediaFileCheck(request);
        this.coverFileCheck(request);
    }

    /**
     * MediaFileCheck
     */
    private void mediaFileCheck(VodUploadRequest request) throws VodClientException {
        String mediaFilePath = request.getMediaFilePath();
        if (StringUtil.isBlank(mediaFilePath)) {
            throw new VodClientException("lack media path");
        }
        if (!FileUtil.isFileExist(mediaFilePath)) {
            throw new VodClientException("media path is invalid");
        }
        if (StringUtil.isBlank(request.getMediaType())) {
            String mediaType = FileUtil.getFileType(mediaFilePath);
            if (StringUtil.isBlank(mediaType)) {
                throw new VodClientException("lack media type");
            }
            request.setMediaType(mediaType);
        }
        if (StringUtil.isBlank(request.getMediaName())) {
            request.setMediaName(FileUtil.getFileName(mediaFilePath));
        }
    }

    /**
     * CoverFileCheck
     */
    private void coverFileCheck(VodUploadRequest request) throws VodClientException {
        String coverFilePath = request.getCoverFilePath();
        if (!StringUtil.isBlank(coverFilePath)) {
            if (!FileUtil.isFileExist(coverFilePath)) {
                throw new VodClientException("cover path is invalid");
            }
            if (StringUtil.isBlank(request.getCoverType())) {
                String coverType = FileUtil.getFileType(coverFilePath);
                if (StringUtil.isBlank(coverType)) {
                    throw new VodClientException("lack cover type");
                }
                request.setCoverType(coverType);
            }
        }
    }

    /**
     * Get index file content
     */
    private String getManifestContent(String mediaFilePath) throws VodClientException {
        String encoding = "UTF-8";
        File file = new File(mediaFilePath);
        long fileLength = file.length();
        if (fileLength > Integer.MAX_VALUE) {
            throw new VodClientException("the file is too large");
        }
        byte[] fileContent = new byte[(int) fileLength];
        try (FileInputStream in = new FileInputStream(file)) {
            int readLength = in.read(fileContent);
            int surplusByte = in.read();
            if (readLength != fileLength || surplusByte != -1) {
                throw new VodClientException("file has changed");
            }
        } catch (FileNotFoundException e) {
            throw new VodClientException("file not found");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            return new String(fileContent, encoding);
        } catch (UnsupportedEncodingException e) {
            throw new VodClientException("file encoding abnormal");
        }
    }

    /**
     * Parse index file, which is compatible with multi-bitrate index files
     */
    private void parseManifest(VodClient vodClient, String manifestFilePath, String manifestMediaType,
                               Set<String> parsedManifestSet, List<String> segmentUrlList) throws Exception {
        if (parsedManifestSet.contains(manifestFilePath)) {
            throw new VodClientException("repeat manifest segment");
        } else {
            parsedManifestSet.add(manifestFilePath);
        }
        String manifestContent = getManifestContent(manifestFilePath);
        ParseStreamingManifestRequest parseStreamingManifestRequest = new ParseStreamingManifestRequest();
        parseStreamingManifestRequest.setMediaManifestContent(manifestContent);
        parseStreamingManifestRequest.setManifestType(manifestMediaType);
        ParseStreamingManifestResponse parseStreamingManifestResponse = parseStreamingManifest(vodClient,
                parseStreamingManifestRequest);
        String[] segmentUrls = parseStreamingManifestResponse.getMediaSegmentSet();
        if (segmentUrls != null) {
            for (String segmentUrl : segmentUrls) {
                String mediaType = FileUtil.getFileType(segmentUrl);
                String mediaFilePath = Paths.get(Paths.get(manifestFilePath).getParent().toString(), segmentUrl)
                        .toString();
                segmentUrlList.add(mediaFilePath);
                if (isManifestMediaType(mediaType)) {
                    parseManifest(vodClient, mediaFilePath, mediaType, parsedManifestSet, segmentUrlList);
                }
            }
        }
    }

    /**
     * Whether to enable an proxy
     */
    private boolean needHttpProxy() {
        return httpProfile != null && (StringUtil.isNotBlank(httpProfile.getProxyHost()));
    }

    private void defaultDomainProxyPort() {
        String proxyHost = httpProfile.getProxyHost();
        // Caller set domain proxy
        if (RegularUtil.letterCheck(proxyHost) && httpProfile.getProxyPort() == 0) {
            // The proxyPort cannot be set based on the protocol method because it is not a cos upload configuration
            logger.info("proxyPort default setting is port 80");
            httpProfile.setProxyPort(80);
        }
    }

    private Boolean isManifestMediaType(String mediaType) {
        return "m3u8".equals(mediaType) || "mpd".equals(mediaType);
    }

    public String getSecretId() {
        return secretId;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getToken() {
        return token;
    }

    /**
     * If you do not set the file information inside the request, please do not set it to false.
     */
    public void setIgnorePreCheck(Boolean ignorePreCheck) {
        this.ignorePreCheck = ignorePreCheck;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        if (retryCount <= 0) {
            retryCount = 1;
            logger.info("RetryCount minimum value is 1");
        }
        this.retryCount = retryCount;
    }

    public void setHttpProfile(HttpProfile httpProfile) {
        this.httpProfile = httpProfile;
    }

}
