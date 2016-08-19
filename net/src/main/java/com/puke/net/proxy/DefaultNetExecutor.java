package com.puke.net.proxy;

import com.google.gson.Gson;
import com.puke.net.VirtualHelper;

/**
 * @author zijiao
 * @version 16/8/19
 */
@SuppressWarnings("unchecked")
public class DefaultNetExecutor implements INetExecutor {

    private static final Gson sGson = new Gson();

    @Override
    public <T> T execute(IRequest request) {
        String response = VirtualHelper.request(request.url(), request.params());
        return (T) sGson.fromJson(response, request.responseCls());
    }

}
