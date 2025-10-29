package com.tanhua.server.interceptor;

import com.tanhua.domain.db.User;
import com.tanhua.server.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 自定义token拦截器
 * 统一处理请求头中token
 */
@Component
@Slf4j
public class TokenInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;
    
    /**
     * 统一处理token
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.debug("token统一处理开始了。。。");
        //1. 拦截器中获取请求头中token
        String headToken = request.getHeader("Authorization");
        //2. 根据token作为key到redis中获取用户对象
        User user = userService.getUser(headToken);
        //3. 用户对象不存在，直接返回没有权限401
        if(user == null){
            response.setStatus(401);//401错误代表用户没有访问权限，需要进行身份认证
            return false;
        }
        //4. 用户对象存在，将用户对象存入ThreadLocal（UserHoler工具类 set get）
        UserHolder.setUser(user);
        return true;
    }
}
