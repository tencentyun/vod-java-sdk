package com.qcloud.vod.model;

import java.lang.reflect.Field;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tencentcloudapi.common.AbstractModel;
import com.tencentcloudapi.vod.v20180717.models.ApplyUploadRequest;

/**
 * 上传请求
 *
 * @author jianguoxu
 */
public class VodUploadRequest extends ApplyUploadRequest {

    private String MediaFilePath;

    private String CoverFilePath;

    private Integer ConcurrentUploadNumber;

    /**
     * cos上传时使用Https上传,默认false
     */
    private Boolean SecureUpload = false;

    public VodUploadRequest() {}

    public VodUploadRequest(String mediaFilePath) {
        this.MediaFilePath = mediaFilePath;
    }

    public VodUploadRequest(String mediaFilePath, String coverFilePath) {
        this(mediaFilePath);
        this.CoverFilePath = coverFilePath;
    }

    public VodUploadRequest(String mediaFilePath, String coverFilePath, String procedure) {
        this(mediaFilePath, coverFilePath);
        this.setProcedure(procedure);
    }

    public String getMediaFilePath() {
        return MediaFilePath;
    }

    public void setMediaFilePath(String mediaFilePath) {
        this.MediaFilePath = mediaFilePath;
    }

    public String getCoverFilePath() {
        return CoverFilePath;
    }

    public void setCoverFilePath(String coverFilePath) {
        this.CoverFilePath = coverFilePath;
    }

    public Integer getConcurrentUploadNumber() {
        return ConcurrentUploadNumber;
    }

    public void setConcurrentUploadNumber(Integer concurrentUploadNumber) {
        this.ConcurrentUploadNumber = concurrentUploadNumber;
    }

    public static String toJsonString(VodUploadRequest obj) {
        return toJsonObject(obj).toString();
    }

    public Boolean getSecureUpload() {
        return SecureUpload;
    }

    public VodUploadRequest openSecureUpload() {
        this.SecureUpload = true;
        return this;
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
        for (Map.Entry<String, JsonElement> entry : jopublic.entrySet()) {
            Object fo = null;
            try {
                Field f = obj.getClass().getDeclaredField(entry.getKey());
                f.setAccessible(true);
                fo = f.get(obj);
            } catch (Exception e) {
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
