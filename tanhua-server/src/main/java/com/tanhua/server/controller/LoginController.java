package com.tanhua.server.controller;

import com.tanhua.domain.db.User;
import com.tanhua.domain.vo.UserInfoVo;
import com.tanhua.server.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 登录控制层
 */
@RestController
@RequestMapping("/user")
@Slf4j //日志注解
public class LoginController {

    @Autowired
    private UserService userService;

    /**
     * 用户保存返回用户id
     * 传智健康Result:响应对象
     * 探花项目ResponseEntity:响应对象
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public ResponseEntity saveUser(@RequestBody User user) {
        Long userId = userService.saveUser(user);
        log.debug("用户保存成功了。。。。" + userId);
        return ResponseEntity.ok(userId);
    }

    /**
     * 根据手机号码查询用户功能
     */
    @RequestMapping(value = "/findByMobile", method = RequestMethod.GET)
    public ResponseEntity findByMobile(String mobile) {
        User user = userService.findByMobile(mobile);
        log.debug("根据手机号码查询用户。。。。" + user.toString());
        return ResponseEntity.ok(user);
    }

    /**
     * 登录注册-发送验证码
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity sendCode(@RequestBody Map<String, String> params) {
        String mobile = params.get("phone");
        userService.sendCode(mobile);
        return ResponseEntity.ok(null);
    }


    /**
     * 登录注册
     */
    @RequestMapping(value = "/loginVerification", method = RequestMethod.POST)
    public ResponseEntity loginReg(@RequestBody Map<String, String> params) {
        String mobile = params.get("phone");
        String verificationCode = params.get("verificationCode");
        //调用业务层登录注册
        Map map = userService.loginReg(mobile,verificationCode);
        /*{
            "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE1NjI4MjkzMzYsInVzZXJfaWQiOiIxIn0.Mbzn6LzsLrkVWEbhexR3lTYDZjxqIcqW11rJxDQ6Ewk",
            "isNew": true  注册true：会跳转完善个人信息页面  登录false：直接跳转首页
        }*/
        return ResponseEntity.ok(map);
    }


    /**
     * 新用户---1填写资料
     */
    @RequestMapping(value = "/loginReginfo", method = RequestMethod.POST)
    public ResponseEntity loginReginfo(@RequestBody UserInfoVo userInfoVo) {
        userService.loginReginfo(userInfoVo);
        return ResponseEntity.ok(null);
    }

    /**
     * 新用户---2选取头像
     */
    @RequestMapping(value = "/loginReginfo/head", method = RequestMethod.POST)
    public ResponseEntity loginReginfoHead(MultipartFile headPhoto) {
        userService.loginReginfoHead(headPhoto);
        return ResponseEntity.ok(null);
    }

}
