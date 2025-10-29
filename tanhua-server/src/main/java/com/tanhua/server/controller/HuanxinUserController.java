package com.tanhua.server.controller;

import com.tanhua.commons.vo.HuanXinUser;
import com.tanhua.server.interceptor.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 环信登录控制层-给app账号（userId）和密码(123456)
 */
@RestController
@RequestMapping("/huanxin")
@Slf4j //日志注解
public class HuanxinUserController {

    /**
     * 用户登录app
     * app会发送请求获取当前用户账号和密码 ==> app短信发送请求获取token ==>后续才可以操作环信功能（例如：聊天 等）
     * @return
     */
    @RequestMapping(value = "/user",method = RequestMethod.GET)
    public ResponseEntity huanxinUser() {
        HuanXinUser huanXinUser = new HuanXinUser(UserHolder.getUserId().toString(),"123456","abc");
        log.debug("app获取用户信息成功了。。。。"+huanXinUser.toString());
        return ResponseEntity.ok(huanXinUser);
    }
}
