package com.qcloud.vod.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.qcloud.cos.internal.Constants;
import com.tencentcloudapi.common.AbstractModel;
import com.tencentcloudapi.vod.v20180717.models.ApplyUploadRequest;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 上传请求
 *
 * @author jianguoxu
 */
public class VodUploadRequest extends ApplyUploadRequest {

    /**
     * 文件路径
     */
    private String mediaFilePath;

    /**
     * 封面路径(可选)
     */
    private String coverFilePath;

    /**
     * 上传并发线程数
     */
    private int concurrentUploadNumber;

    /**
     * 文件达到多大才开始分片上传(单位:byte,默认5mb)
     */
    private long multipartUploadThreshold = 5 * Constants.MB;

    /**
     * 分片时每一片的大小(单位:byte,默认5mb)
     */
    private long minimumUploadPartSize = 5 * Constants.MB;

    /**
     * cos上传时使用Https上传,默认false
     */
    private boolean secureUpload;

    /**
     * 自定义请求头
     */
    private Map<String,String> headersMap;

    public VodUploadRequest() {}

    public VodUploadRequest(String mediaFilePath) {
        this.mediaFilePath = mediaFilePath;
    }

    public VodUploadRequest(String mediaFilePath, String coverFilePath) {
        this(mediaFilePath);
        this.coverFilePath = coverFilePath;
    }

    public VodUploadRequest(String mediaFilePath, String coverFilePath, String procedure) {
        this(mediaFilePath, coverFilePath);
        this.setProcedure(procedure);
    }

    public String getMediaFilePath() {
        return mediaFilePath;
    }

    public void setMediaFilePath(String mediaFilePath) {
        this.mediaFilePath = mediaFilePath;
    }

    public String getCoverFilePath() {
        return coverFilePath;
    }

    public void setCoverFilePath(String coverFilePath) {
        this.coverFilePath = coverFilePath;
    }

    public int getConcurrentUploadNumber() {
        return concurrentUploadNumber;
    }

    public void setConcurrentUploadNumber(Integer concurrentUploadNumber) {
        this.concurrentUploadNumber = concurrentUploadNumber;
    }

    public long getMultipartUploadThreshold() {
        return multipartUploadThreshold;
    }

    public void setMultipartUploadThreshold(long multipartUploadThreshold) {
        if (multipartUploadThreshold > 0) {
            this.multipartUploadThreshold = multipartUploadThreshold;
        }
    }

    public long getMinimumUploadPartSize() {
        return minimumUploadPartSize;
    }

    public void setMinimumUploadPartSize(long minimumUploadPartSize) {
        if (minimumUploadPartSize > 0) {
            this.minimumUploadPartSize = minimumUploadPartSize;
        }
    }

    public boolean needSecureUpload() {
        return secureUpload;
    }

    public void enableSecureUpload() {
        this.secureUpload = true;
    }

    public void putRequestHeader(String name,String value) {
        if (this.headersMap == null) {
            this.headersMap = new HashMap<>();
        }
        this.headersMap.put(name,value);
    }

    public Map<String,String> getRequestHeader() {
        if (this.headersMap != null) {
            return Collections.unmodifiableMap(this.headersMap);
        }
        return null;
    }

    public static String toJsonString(VodUploadRequest obj) {
        return toJsonObject(obj).toString();
    }

    private static <O extends AbstractModel> JsonObject toJsonObject(O obj) {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        JsonObject joall = new JsonObject();
        JsonObject joadd = gson.toJsonTree(obj.any()).getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : joadd.entrySet()) {
            joall.add(entry.getKey(), entry.getValue());
        }
        // jopublic will override joadd if key conflict exists
        JsonObject jopublic = gson.toJsonTree(obj).getAsJsonObject();
        Set<String> fieldNameSet = new HashSet<>();
        for (Field field : obj.getClass().getFields()) {
            fieldNameSet.add(field.getName());
        }
        for (Map.Entry<String, JsonElement> entry : jopublic.entrySet()) {
            Object fo = null;
            Field f;
            try {
                if (fieldNameSet.contains(entry.getKey())) {
                    f = obj.getClass().getDeclaredField(entry.getKey());
                    f.setAccessible(true);
                    fo = f.get(obj);
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                // this should never happen
            }
            if (fo instanceof AbstractModel) {
                joall.add(entry.getKey(), toJsonObject((AbstractModel)fo));
            } else {
                joall.add(entry.getKey(), entry.getValue());
            }
        }
        return joall;
    }
}
