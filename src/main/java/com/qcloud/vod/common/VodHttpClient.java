package com.qcloud.vod.common;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * 资源请求对象
 *
 * @author alanyfwu
 */
public class VodHttpClient {

    private static final Logger logger = LoggerFactory.getLogger(VodHttpClient.class);

    private CloseableHttpClient httpclient;

    private CloseableHttpResponse execute;

    private final RequestConfig requestConfig;

    private InputStream inputStream;

    private final Iterator<String> urlIterator;

    /**
     * @param timeout 单位: 秒
     * @param urls    资源url
     */
    public VodHttpClient(int timeout, String... urls) {
        this(timeout, Arrays.asList(urls));
    }

    public VodHttpClient(int timeout, List<String> urlList) {
        this.urlIterator = urlList.iterator();
        this.requestConfig = RequestConfig.custom().setConnectTimeout(1000 * timeout).build();
    }

    public InputStreamInfo getNextInputStream() throws IOException {
        this.executePre();
        if (this.urlIterator.hasNext()) {
            String url = this.urlIterator.next();
            // 跳过空url资源
            while (StringUtils.isBlank(url) && this.urlIterator.hasNext()) {
                url = this.urlIterator.next();
            }
            if (StringUtils.isBlank(url)) {
                this.close();
                return null;
            }
            HttpGet httpGet = new HttpGet(url);
            httpGet.setConfig(this.requestConfig);
            this.execute = this.httpclient.execute(httpGet);
            HttpEntity entity = this.execute.getEntity();
            this.inputStream = entity.getContent();
            return new InputStreamInfo(this.inputStream, entity.getContentLength());
        }
        logger.info("url has been consumed");
        this.close();
        return null;
    }

    public void close() {
        try {
            if (this.inputStream != null) {
                this.inputStream.close();
            }
            if (this.execute != null) {
                this.execute.close();
            }
            if (this.httpclient != null) {
                this.httpclient.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void executePre() {
        if (this.httpclient == null) {
            this.httpclient = HttpClients.createDefault();
        }
        if (this.inputStream != null) {
            try {
                this.inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (this.execute != null) {
            try {
                this.execute.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class InputStreamInfo {

        private final InputStream inputStream;

        private final long contentLength;

        public InputStreamInfo(InputStream inputStream, long contentLength) {
            this.inputStream = inputStream;
            this.contentLength = contentLength;
        }

        public InputStream getInputStream() {
            return this.inputStream;
        }

        public long getContentLength() {
            return this.contentLength;
        }

    }

}
