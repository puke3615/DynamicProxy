package com.puke.net;

import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * @author zijiao
 * @version 16/8/19
 */
public class HttpHelper {

    public static String request(String url, Map<String, Object> params) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            if (params != null && params.size() > 0) {
                for (String key : params.keySet()) {
                    connection.setRequestProperty(key, params.get(key).toString());
                }
            }
            InputStream is = connection.getInputStream();
            return is2Str(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String is2Str(InputStream is) throws IOException {
        try {
            byte[] buffer = new byte[1024];
            int index = -1;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while ((index = is.read(buffer)) != -1) {
                baos.write(buffer, 0, index);
            }
            return new String(baos.toByteArray());
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

}
