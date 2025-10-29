package com.tanhua.dubbo.api.db;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.domain.db.User;
import com.tanhua.dubbo.mapper.UserMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * 服务提供者 接口实现类
 */
@Service
public class UserApiImpl implements UserApi{

    @Autowired
    private UserMapper userMapper;

    /**
     * 用户保存返回用户id
     * @param user
     * @return
     */
    @Override
    public Long saveUser(User user) {
        /*Date nowDate = new Date();
        user.setUpdated(nowDate);
        user.setCreated(nowDate);*/
        userMapper.insert(user);
        return user.getId();
    }

    @Override
    public User findByMobile(String mobile) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("mobile",mobile);
        return userMapper.selectOne(queryWrapper);
    }
}
