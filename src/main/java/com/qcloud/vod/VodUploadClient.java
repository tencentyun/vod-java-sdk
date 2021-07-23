package com.qcloud.vod;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.BasicSessionCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.transfer.TransferManager;
import com.qcloud.cos.transfer.Upload;
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
import com.tencentcloudapi.vod.v20180717.VodClient;
import com.tencentcloudapi.vod.v20180717.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

	private String secretId;

	private String secretKey;

	private String token;

	private Boolean ignoreCheck;

	private Integer retryTime;

	private HttpProfile httpProfile = null;

	public VodUploadClient(String secretId, String secretKey) {
		this(secretId, secretKey, "");
	}

	public VodUploadClient(String secretId, String secretKey, String token) {
		this.secretId = secretId;
		this.secretKey = secretKey;
		this.token = token;
		this.ignoreCheck = false;
		this.retryTime = 3;
	}

	public VodUploadClient(String secretId, String secretKey, HttpProfile httpProfile) {
		this(secretId, secretKey);
		this.httpProfile = httpProfile;
	}

	/**
	 * Upload
	 */
	public VodUploadResponse upload(String region, VodUploadRequest request) throws Exception {
		if (!ignoreCheck) {
			prefixCheckAndSetDefaultVal(region, request);
		}

		Credential credential = new Credential(secretId, secretKey, token);
		VodClient vodClient = null;
		if (httpProfile != null && httpProfile.getProxyHost() != "" && httpProfile.getProxyPort() != 0) {
			vodClient = new VodClient(credential, region, new ClientProfile(ClientProfile.SIGN_TC3_256, httpProfile));
		} else {
			vodClient = new VodClient(credential, region);
		}

		Set<String> parsedManifestSet = new HashSet<String>();
		List<String> segmentUrlList = new ArrayList<String>();

		if (isManifestMediaType(request.getMediaType())) {
			parseManifest(vodClient, request.getMediaFilePath(), request.getMediaType(), parsedManifestSet,
					segmentUrlList);
		}

		ApplyUploadRequest applyUploadRequest = ApplyUploadRequest
				.fromJsonString(VodUploadRequest.toJsonString(request), ApplyUploadRequest.class);
		ApplyUploadResponse applyUploadResponse = applyUpload(vodClient, applyUploadRequest);
		logger.info("ApplyUpload Response = {}", PrintUtil.PrintObject(applyUploadResponse));

		COSCredentials credentials = null;
		if (applyUploadResponse.getTempCertificate() != null) {
			TempCertificate certificate = applyUploadResponse.getTempCertificate();
			credentials = new BasicSessionCredentials(certificate.getSecretId(), certificate.getSecretKey(),
					certificate.getToken());
		} else {
			credentials = new BasicCOSCredentials(secretId, secretKey);
		}
		ClientConfig clientConfig = new ClientConfig(new Region(applyUploadResponse.getStorageRegion()));
		if (request.getSecureUpload()) {
		    clientConfig.setHttpProtocol(HttpProtocol.https);
		}
		if (httpProfile != null && httpProfile.getProxyHost() != "" && httpProfile.getProxyPort() != 0) {
			clientConfig.setHttpProxyIp(httpProfile.getProxyHost());
			clientConfig.setHttpProxyPort(httpProfile.getProxyPort());
			if (httpProfile.getProxyUsername() != "") {
				clientConfig.setProxyUsername(httpProfile.getProxyUsername());
				clientConfig.setProxyPassword(httpProfile.getProxyPassword());
				clientConfig.setUseBasicAuth(true);
			}
		}

		COSClient cosClient = new COSClient(credentials, clientConfig);

		TransferManager transferManager = null;
		if (request.getConcurrentUploadNumber() != null && request.getConcurrentUploadNumber() > 0) {
			ExecutorService fixedThreadPool = Executors.newFixedThreadPool(request.getConcurrentUploadNumber());
			transferManager = new TransferManager(cosClient, fixedThreadPool);
		} else {
			transferManager = new TransferManager(cosClient);
		}

		if (StringUtil.isNotEmpty(request.getMediaType())
				&& StringUtil.isNotEmpty(applyUploadResponse.getMediaStoragePath())) {
			uploadCos(transferManager, request.getMediaFilePath(), applyUploadResponse.getStorageBucket(),
					applyUploadResponse.getMediaStoragePath());
		}
		if (StringUtil.isNotEmpty(request.getCoverType())
				&& StringUtil.isNotEmpty(applyUploadResponse.getCoverStoragePath())) {
			uploadCos(transferManager, request.getCoverFilePath(), applyUploadResponse.getStorageBucket(),
					applyUploadResponse.getCoverStoragePath());
		}

		for (String segmentUrl : segmentUrlList) {
			String segmentFilePath = Paths.get(segmentUrl).toString().replace('\\', '/');
			String cosDir = Paths.get(applyUploadResponse.getMediaStoragePath()).getParent().toString();
			String parentPath = Paths.get(request.getMediaFilePath()).getParent().toString();
			String segmentPath = segmentUrl.substring(parentPath.length());
			String segmentStoragePath = Paths.get(cosDir, segmentPath).toString().replace('\\', '/');

			uploadCos(transferManager, segmentFilePath, applyUploadResponse.getStorageBucket(),
					segmentStoragePath.substring(1));
		}

		transferManager.shutdownNow();

		CommitUploadRequest commitUploadRequest = new CommitUploadRequest();
		commitUploadRequest.setVodSessionKey(applyUploadResponse.getVodSessionKey());
		commitUploadRequest.setSubAppId(request.getSubAppId());
		CommitUploadResponse commitUploadResponse = commitUpload(vodClient, commitUploadRequest);
		logger.info("CommitUpload Response = {}", PrintUtil.PrintObject(commitUploadResponse));

		VodUploadResponse uploadResponse = null;
		try {
			uploadResponse = CopyUtil.clone(commitUploadResponse, VodUploadResponse.class);
		} catch (Exception e) {
			throw new VodClientException(e);
		}

		return uploadResponse;
	}

	public VodUploadResponse upload(final String region, final VodUploadRequest request, int timeout) throws Exception {
		final VodUploadClient vodUploadClient = this;

		Callable<VodUploadResponse> task = new Callable<VodUploadResponse>() {
			public VodUploadResponse call() throws Exception {
				VodUploadResponse response = vodUploadClient.upload(region, request);
				return response;
			}
		};

		ExecutorService service = Executors.newSingleThreadExecutor();
		Future<VodUploadResponse> future = service.submit(task);
		try {
			VodUploadResponse response = future.get(timeout, TimeUnit.SECONDS);
			return response;
		} catch (Exception e) {
			throw new VodClientException(e);
		} finally {
			service.shutdown();
		}
	}

	/**
	 * COS upload
	 */
	private void uploadCos(TransferManager transferManager, String localPath, String bucket, String cosPath)
			throws Exception {
		File file = new File(localPath);
		try {
			Upload upload = transferManager.upload(bucket, cosPath, file);
			upload.waitForCompletion();
		} catch (Exception e) {
			logger.error("Upload Cos Err", e);
			throw e;
		}
	}

	/**
	 * Apply for upload
	 */
	private ApplyUploadResponse applyUpload(VodClient client, ApplyUploadRequest request) throws Exception {
		TencentCloudSDKException err = null;
		for (int i = 0; i < retryTime; i++) {
			try {
				ApplyUploadResponse response = client.ApplyUpload(request);
				return response;
			} catch (TencentCloudSDKException exception) {
				if (StringUtil.isEmpty(exception.getRequestId())) {
					err = exception;
					continue;
				}
				throw exception;
			}
		}
		throw err;
	}

	/**
	 * Confirm upload
	 */
	private CommitUploadResponse commitUpload(VodClient client, CommitUploadRequest request) throws Exception {
		TencentCloudSDKException err = null;
		for (int i = 0; i < retryTime; i++) {
			try {
				CommitUploadResponse response = client.CommitUpload(request);
				return response;
			} catch (TencentCloudSDKException e) {
				if (StringUtil.isEmpty(e.getRequestId())) {
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
	private ParseStreamingManifestResponse parseStreamingManifest(VodClient client,
			ParseStreamingManifestRequest request) throws Exception {
		TencentCloudSDKException err = null;
		for (int i = 0; i < retryTime; i++) {
			try {
				ParseStreamingManifestResponse response = client.ParseStreamingManifest(request);
				return response;
			} catch (TencentCloudSDKException e) {
				if (StringUtil.isEmpty(e.getRequestId())) {
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
		if (StringUtil.isEmpty(region)) {
			throw new VodClientException("lack region");
		}

		if (StringUtil.isEmpty(request.getMediaFilePath())) {
			throw new VodClientException("lack media path");
		}
		if (!FileUtil.isFileExist(request.getMediaFilePath())) {
			throw new VodClientException("media path is invalid");
		}
		if (StringUtil.isEmpty(request.getMediaType())) {
			String mediaType = FileUtil.getFileType(request.getMediaFilePath());
			if ("".equals(mediaType)) {
				throw new VodClientException("lack media type");
			}
			request.setMediaType(mediaType);
		}
		if (StringUtil.isEmpty(request.getMediaName())) {
			request.setMediaName(FileUtil.getFileName(request.getMediaFilePath()));
		}

		if (!StringUtil.isEmpty(request.getCoverFilePath())) {
			if (!FileUtil.isFileExist(request.getCoverFilePath())) {
				throw new VodClientException("cover path is invalid");
			}
			if (StringUtil.isEmpty(request.getCoverType())) {
				String coverType = FileUtil.getFileType(request.getCoverFilePath());
				if ("".equals(coverType)) {
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
		Long filelength = file.length();
		byte[] filecontent = new byte[filelength.intValue()];
		try {
			FileInputStream in = new FileInputStream(file);
			in.read(filecontent);
			in.close();
		} catch (FileNotFoundException e) {
			throw new VodClientException("file not found");
		} catch (IOException e) {
			throw new VodClientException("file read failed");
		}
		try {
			return new String(filecontent, encoding);
		} catch (UnsupportedEncodingException e) {
			throw new VodClientException("file encoding abnormal");
		}
	}

	/**
	 * Parse index file, which is compatible with multi-bitrate index files
	 */
	private void parseManifest(VodClient vodClient, String manifestFilePath, String manifestMediaType, Set<String> parsedManifestSet, List<String> segmentUrlList) throws Exception {
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

	private Boolean isManifestMediaType(String mediaType) {
		if (mediaType.equals("m3u8") || mediaType.equals("mpd")) {
			return true;
		}

		return false;
	}

	public String getSecretId() {
		return secretId;
	}

	public void setSecretId(String secretId) {
		this.secretId = secretId;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public Boolean getIgnoreCheck() {
		return ignoreCheck;
	}

	public void setIgnoreCheck(Boolean ignoreCheck) {
		this.ignoreCheck = ignoreCheck;
	}

	public Integer getRetryTime() {
		return retryTime;
	}

	public void setRetryTime(Integer retryTime) {
		this.retryTime = retryTime;
	}

	public HttpProfile getHttpProfile() {
		return httpProfile;
	}

	public void setHttpProfile(HttpProfile httpProfile) {
		this.httpProfile = httpProfile;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
