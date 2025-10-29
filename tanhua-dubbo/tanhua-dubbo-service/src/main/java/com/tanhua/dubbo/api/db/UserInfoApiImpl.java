package com.tanhua.dubbo.api.db;

import com.tanhua.domain.db.UserInfo;
import com.tanhua.dubbo.mapper.UserInfoMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * 服务提供者 接口实现类
 */
@Service
public class UserInfoApiImpl implements UserInfoApi {

    @Autowired
    private UserInfoMapper userInfoMapper;

    /**
     * 保存个人信息
     *
     * @param userInfo
     */
    @Override
    public void saveUserInfo(UserInfo userInfo) {
       /* Date nowDate = new Date();
        userInfo.setUpdated(nowDate);
        userInfo.setCreated(nowDate);*/
        userInfoMapper.insert(userInfo);
    }

    /**
     * 根据用户id更新用户信息
     *
     * @param userInfo
     */
    @Override
    public void editUserInfo(UserInfo userInfo) {
        userInfoMapper.updateById(userInfo);
    }

    /**
     * 根据用户id查询用户信息
     * @param userId
     * @return
     */
    @Override
    public UserInfo queryUserInfo(Long userId) {
        return userInfoMapper.selectById(userId);
    }
}
