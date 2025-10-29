package com.tanhua.server.service;

import com.sun.deploy.util.BlackList;
import com.tanhua.domain.db.Question;
import com.tanhua.domain.db.Settings;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.SettingsVo;
import com.tanhua.dubbo.api.db.BlackListApi;
import com.tanhua.dubbo.api.db.QuestionApi;
import com.tanhua.dubbo.api.db.SettingsApi;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 通用设置业务处理层
 */
@Service
public class SettingsService {

    @Reference
    private QuestionApi questionApi;

    @Reference
    private SettingsApi settingsApi;

    @Reference
    private BlackListApi blackListApi;

    /**
     * 通用设置查询
     */
    public SettingsVo querySettings() {
        //定义需要返回SettingsVo
        SettingsVo vo = new SettingsVo();

        //a.通过UserHolder.getUserId()   UserHolder.getUser().getMobile()
        Long userId = UserHolder.getUserId(); //获取当前登录用户id
        String mobile = UserHolder.getUser().getMobile();//获取手机号码
        //b.根据用户id查询问题表   问题不存在，设置默认问题
        Question question = questionApi.queryByUserId(userId);
        String strangerQuestion = "约吗?";//返回vo内容
        if(question != null && !StringUtils.isEmpty(question.getTxt())){
            strangerQuestion = question.getTxt();
        }

        //c.根据用户id查询通用设置表， 记录不存在 返回默认不通知
        Settings settings = settingsApi.queryByUserId(userId);
        if(settings == null){
            //如果记录不存在 全部设置为不通知消息
            vo.setLikeNotification(false);
            vo.setPinglunNotification(false);
            vo.setGonggaoNotification(false);
        }
        else
        {
            BeanUtils.copyProperties(settings,vo);
        }
        //d.封装数据到返回Vo对象中
        vo.setStrangerQuestion(strangerQuestion);//问题内容
        vo.setPhone(mobile);//手机号
        return vo;

    }
    /**
     * 通用设置修改
     */
    public void updateOrSaveSettings(boolean likeNotification, boolean pinglunNotification, boolean gonggaoNotification) {
        Long userId = UserHolder.getUserId();
        //a.根据当前用户id查询通知设置表记录是否存在
        Settings settings = settingsApi.queryByUserId(userId);
        //b.如果不存在，则保存通用设置记录
        if(settings == null){
            settings = new Settings();
            settings.setUserId(userId);//当前用户id
            settings.setLikeNotification(likeNotification);//喜欢通知
            settings.setPinglunNotification(pinglunNotification);//评论通知
            settings.setGonggaoNotification(gonggaoNotification);//公共通知
            settingsApi.saveSettings(settings);
        }
        else {
            //c.如果存在，则更新通用设置记录
            settings.setUserId(userId);//当前用户id
            settings.setLikeNotification(likeNotification);//喜欢通知
            settings.setPinglunNotification(pinglunNotification);//评论通知
            settings.setGonggaoNotification(gonggaoNotification);//公共通知
            settingsApi.updateSettings(settings);
        }
    }

    /**
     * 设置陌生人问题 -保存 更新
     */
    public void updateOrSaveQuestion(String txt) {
        Long userId = UserHolder.getUserId();
        //a.根据用户id查询问题表记录是否存在
        Question question = questionApi.queryByUserId(userId);
        //b.不存在，则保存问题表记录
        if(question == null){
            question  = new Question();
            question.setUserId(userId);//当前用户id
            question.setTxt(txt);//修改后问题
            questionApi.saveQuestion(question);
        }
        else {
            //c.存在，则更新问题表
            question.setTxt(txt);//修改后问题
            questionApi.updateQuestion(question);
        }
    }

    /**
     * 黑名单 - 翻页列表
     */
    public PageResult queryBlacklist(int page, int pagesize) {
        Long userId = UserHolder.getUserId();
        //根据当前页面 每页记录数 当前用户id 查询黑名单列表用户
        PageResult<UserInfo> pageResult = blackListApi.queryBlacklist(page,pagesize,userId);
        return pageResult;
    }

    /**
     * 黑名单 - 移除
     */
    public void delBlacklist(Long blackUserId) {
        blackListApi.delBlacklist(blackUserId,UserHolder.getUserId());
    }
}
