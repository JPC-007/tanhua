package com.tanhua.manage.interceptor;

import com.tanhua.manage.domain.Admin;
import com.tanhua.manage.service.AdminService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 自定义拦截器
 *    1、实现HandlerInterceptor接口
 *    2、实现接口中的三个方法（根据需求去实现）
 *      * preHandler  ：前置处理
 *        poistHandler：后置处理
 *        afterCompletion：页面加载前处理
 *    3、完成拦截器注册（自定义配置类）
 */
@Component
public class TokenInterceptor implements HandlerInterceptor {

    @Autowired
    private AdminService adminService;

    /**
     * 前置处理：进入controller方法之前
     * 返回值：
     *  true：正常，进入到controller方法
     *  false：拦截
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1、获取头信息
        String token = request.getHeader("Authorization");
        token = token.replace("Bearer ", "");

        //2、如果没有token，权限不足 （返回false）
        if(StringUtils.isEmpty(token)) {
            //token为null，返回状态码 401
            response.setStatus(401);
            return false;
        }
        //3、调用service根据token查询用户
        Admin admin = adminService.getByToken(token);
        if(admin == null) {
            //token为null，返回状态码 401
            response.setStatus(401);
            return false;
        }
        //4、将对象存入redis
        AdminHolder.set(admin);
        return true;
    }
}
