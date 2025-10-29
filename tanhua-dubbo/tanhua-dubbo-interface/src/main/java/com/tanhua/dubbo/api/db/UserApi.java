package com.tanhua.dubbo.api.db;

import com.tanhua.domain.db.User;

/**
 * 服务提供者 接口
 */
public interface UserApi {
    /**
     * 用户保存返回用户id
     * 传智健康Result:响应对象
     * 探花项目ResponseEntity:响应对象
     */
    Long saveUser(User user);
    /**
     * 根据手机号码查询用户功能
     */
    User findByMobile(String mobile);
}
