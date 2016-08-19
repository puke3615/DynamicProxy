package com.puke.dynamicproxy;

import com.puke.net.User;
import com.puke.net.proxy.Param;
import com.puke.net.proxy.URL;

/**
 * @author zijiao
 * @version 16/8/19
 */
@URL("http://***.***.***")
public interface LoginApi {

    User login(@Param("username") String username,
               @Param("password") String password);

}
