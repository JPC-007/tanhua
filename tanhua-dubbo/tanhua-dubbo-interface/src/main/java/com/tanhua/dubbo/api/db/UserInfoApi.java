package com.tanhua.dubbo.api.db;

import com.tanhua.domain.db.UserInfo;

/**
 * 个人信息服务接口
 */
public interface UserInfoApi {
    /**
     * 保存个人信息
     * @param userInfo
     */
    void saveUserInfo(UserInfo userInfo);

    /**
     * 根据用户id更新手机号码
     * @param userInfo
     */
    void editUserInfo(UserInfo userInfo);

    /**
     * 查询用户信息
     */
    UserInfo queryUserInfo(Long userId);
}
