package com.puke.net.proxy;

import java.util.Map;

/**
 * @author zijiao
 * @version 16/8/19
 */
public class Request implements IRequest {

    String url;
    Map<String, Object> params;
    Class<?> responseCls;

    public Request(String url, Map<String, Object> params, Class<?> responseCls) {
        this.url = url;
        this.params = params;
        this.responseCls = responseCls;
    }

    @Override
    public String url() {
        return url;
    }

    @Override
    public Map<String, Object> params() {
        return params;
    }

    @Override
    public Class<?> responseCls() {
        return responseCls;
    }

}
