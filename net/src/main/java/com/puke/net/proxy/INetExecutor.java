package com.puke.net.proxy;

/**
 * @author zijiao
 * @version 16/8/19
 */
public interface INetExecutor {

    <T> T execute(IRequest request);

}
