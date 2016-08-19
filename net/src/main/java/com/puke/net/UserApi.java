package com.puke.net;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zijiao
 * @version 16/8/19
 */
public class UserApi {

    private static final String API_LOGIN = "http://***.***.***";
    private static final Gson sGson = new Gson();

    public static User login(String username, String password) {
        Map<String, Object> params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);
        String response = VirtualHelper.request(API_LOGIN, params);
        //注，这里只是为了举例说明一下，就假设此时的数据结构就是跟User一致的
        return sGson.fromJson(response, User.class);
    }

}
