package com.puke.net;

import com.google.gson.Gson;

import java.util.Map;

/**
 * @author zijiao
 * @version 16/8/19
 */
public class VirtualHelper {

    private static final Gson sGson = new Gson();

    public static String request(String url, Map<String, Object> params) {
        if (params != null) {
            if ("123".equals(params.get("username"))
                    && "456".equals(params.get("password"))) {
                User user = new User();
                user.address = "杭州";
                user.sex = "男";
                user.uId = "Id";
                user.username = "啊啊";
                return sGson.toJson(user);
            }
        }
        return null;
    }

}
