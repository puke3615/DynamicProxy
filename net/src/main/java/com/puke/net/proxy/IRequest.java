package com.puke.net.proxy;

import java.util.Map;

/**
 * @author zijiao
 * @version 16/8/19
 */
public interface IRequest {

    String url();

    Map<String, Object> params();

    Class<?> responseCls();

}
