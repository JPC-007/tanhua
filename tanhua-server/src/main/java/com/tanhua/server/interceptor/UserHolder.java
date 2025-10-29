package com.tanhua.server.interceptor;

import com.tanhua.domain.db.User;

/**
 * 登陆用户信息持有者
 * 通过ThreadLocal的形式，存储登陆用户的数据
 */
public class UserHolder {

    private static ThreadLocal<User> userThreadLocal = new ThreadLocal<User>();

    /**
     * 向当前线程中存入用户数据
     * @param user
     */
    public static void setUser(User user){
        userThreadLocal.set(user);
    }

    /**
     * 从当前线程中获取用户数据
     * @return
     */
    public static User getUser(){
        return userThreadLocal.get();
    }

    /**
     * 获取登陆用户的id
     * @return
     */
    public static Long getUserId(){
        return userThreadLocal.get().getId();
    }
}