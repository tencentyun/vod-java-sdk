package com.qcloud.vod.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.qcloud.vod.common.StringUtil;
import com.tencentcloudapi.common.AbstractModel;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Url上传请求,视频资源和封面可以混合本地文件与Url网络资源
 *
 * @author alanyfwu
 */
public class VodUrlUploadRequest extends VodUploadRequest {

    private String mediaUrl;

    private String coverUrl;

    private InputStream mediaInputStream;

    private long mediaContentLength;

    private InputStream coverInputStream;

    private long coverContentLength;

    public VodUrlUploadRequest(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public VodUrlUploadRequest(String mediaUrl, String coverUrl) {
        super();
        this.mediaUrl = mediaUrl;
        this.coverUrl = coverUrl;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public InputStream getMediaInputStream() {
        return mediaInputStream;
    }

    public InputStream getCoverInputStream() {
        return coverInputStream;
    }

    public long getMediaContentLength() {
        return mediaContentLength;
    }

    public long getCoverContentLength() {
        return coverContentLength;
    }

    /**
     * 初始化网络流式资源
     */
    public void initUrlResources() throws IOException {
        CloseableHttpClient httpclient = null;
        HttpGet httpGet;
        CloseableHttpResponse execute = null;
        try {
            httpclient = HttpClients.createDefault();
            if (StringUtil.isNotBlank(mediaUrl)) {
                httpGet = new HttpGet(mediaUrl);
                execute = httpclient.execute(httpGet);
                HttpEntity entity = execute.getEntity();
                mediaContentLength = entity.getContentLength();
                byte[] bytes = EntityUtils.toByteArray(entity);
                this.mediaInputStream = new ByteArrayInputStream(bytes);
            }
            if (StringUtil.isNotBlank(coverUrl)) {
                httpGet = new HttpGet(coverUrl);
                execute = httpclient.execute(httpGet);
                HttpEntity entity = execute.getEntity();
                coverContentLength = entity.getContentLength();
                byte[] bytes = EntityUtils.toByteArray(entity);
                this.coverInputStream = new ByteArrayInputStream(bytes);
            }
        } finally {
            if (execute != null) {
               execute.close();
            }
            if (httpclient != null) {
                httpclient.close();
            }
        }
    }

    public static String toJsonString(VodUrlUploadRequest obj) {
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