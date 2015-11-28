package io.loli.box.service.impl;

import com.qiniu.api.auth.AuthException;
import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.io.IoApi;
import com.qiniu.api.io.PutExtra;
import com.qiniu.api.io.PutRet;
import com.qiniu.api.rs.PutPolicy;
import com.qiniu.api.rs.RSClient;
import io.loli.box.service.AbstractStorageService;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author choco
 */
public class QiniuStorageService extends AbstractStorageService {


    @Value("${storage.qiniu.url}")
    private String url;
    @Value("${storage.qiniu.key}")
    private String key;
    @Value("${storage.qiniu.secret}")
    private String secret;
    @Value("${storage.qiniu.name}")
    private String name;

    private Mac mac;

    @PostConstruct
    private void init() {
        mac = new Mac(key, secret);
    }

    @Override
    public String upload(InputStream is, String filename, String contentType, long length) throws IOException {
        PutPolicy putPolicy = new PutPolicy(name);
        String uptoken = null;
        try {
            uptoken = putPolicy.token(mac);
        } catch (AuthException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        PutExtra extra = new PutExtra();
        if (StringUtils.isNotBlank(contentType)) {
            extra.mimeType = contentType;
        }
        PutRet ret = IoApi.Put(uptoken, filename, is, extra);
        return ret.getKey();
    }

    @Override
    public void deleteFile(String filename) {
        RSClient client = new RSClient(mac);
        client.delete(name, filename);
    }
}
