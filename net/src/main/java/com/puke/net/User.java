package com.puke.net;

/**
 * @author zijiao
 * @version 16/8/19
 */
public class User {

    public String username;
    public String uId;
    public String sex;
    public String address;

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", uId='" + uId + '\'' +
                ", sex='" + sex + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
