package com.tanhua.server.service;

import com.alibaba.fastjson.JSON;
import com.tanhua.commons.templates.HuanXinTemplate;
import com.tanhua.domain.db.Question;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.RecommendUserQueryParam;
import com.tanhua.domain.vo.TodayBestVo;
import com.tanhua.dubbo.api.db.QuestionApi;
import com.tanhua.dubbo.api.db.UserInfoApi;
import com.tanhua.dubbo.api.mongo.RecommendUserApi;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.commons.lang3.RandomUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 佳人业务处理层
 */
@Service
public class TodayBestService {
    @Reference
    private RecommendUserApi recommendUserApi;

    @Reference
    private UserInfoApi userInfoApi;

    @Reference
    private QuestionApi questionApi;

    @Autowired
    private HuanXinTemplate huanXinTemplate;

    /**
     * 今日佳人
     */
    public TodayBestVo todayBest() {
        Long userId = UserHolder.getUserId();
        //1.定义返回TodayBestVo
        TodayBestVo vo  = new TodayBestVo();
        //2.根据当前用户id查询跟当前用户最匹配的今日佳人用户对象RecommendUser(UserId Score)
        //db.getCollection("recommend_user").find({toUserId:1}).sort({"score":-1}).limit(1)
        RecommendUser recommendUser =  recommendUserApi.queryMaxScore(userId);
        //假设recommendUser为空 设置默认用户
        if(recommendUser == null){
            recommendUser = new RecommendUser();
            recommendUser.setScore(88d);
            recommendUser.setUserId(6l);
        }
        //3.根据今日佳人UserId查询tb_userInfo表得到UserInfo对象
        UserInfo userInfo = userInfoApi.queryUserInfo(recommendUser.getUserId());
        //4.将UserInfo与Score 封装Vo返回
        BeanUtils.copyProperties(userInfo,vo);
        if(!StringUtils.isEmpty(userInfo.getTags())){
            vo.setTags(userInfo.getTags().split(","));
        }
        //设置缘分值
        vo.setFateValue(recommendUser.getScore().longValue());
        return vo;
    }

    /**
     * 推荐用户列表（推荐佳人列表）
     */
    public PageResult<TodayBestVo> recommendation(RecommendUserQueryParam param) {
        //定义返回PageResult<TodayBestVo>
        PageResult<TodayBestVo> todayBestVoPageResult = new PageResult<>();
        //获取当前用户id
        Long userId = UserHolder.getUserId();
        //a.根据用户id分页查询推荐用户列表
        PageResult<RecommendUser> pageResult = recommendUserApi.queryPageByUserId(param.getPage(),param.getPagesize(),userId);
        List<RecommendUser> recommendUserList = null;
        //b.如果推荐用户列表数据为空，造10条假数据
        if(pageResult == null || CollectionUtils.isEmpty(pageResult.getItems())){
            //造数据
            recommendUserList = defaultRecommend();
        }
        else
        {
            recommendUserList = pageResult.getItems();
        }
        //c.根据推荐用户ids查询用户信息
        List<TodayBestVo> todayBestVoList = new ArrayList<>();
        for (RecommendUser recommendUser : recommendUserList) {
            TodayBestVo vo = new TodayBestVo();
            Long recommendUserId = recommendUser.getUserId();//推荐用户的id
            UserInfo userInfo = userInfoApi.queryUserInfo(recommendUserId);//推荐用户信息
            //4.将UserInfo与Score 封装Vo返回
            BeanUtils.copyProperties(userInfo,vo);
            if(!StringUtils.isEmpty(userInfo.getTags())){
                vo.setTags(userInfo.getTags().split(","));
            }
            //设置缘分值
            vo.setFateValue(recommendUser.getScore().longValue());
            todayBestVoList.add(vo);
        }

        //d.封装VO，返回给app
        //copy分页数据
        BeanUtils.copyProperties(pageResult,todayBestVoPageResult);
        //copy当前页面展示的数据
        todayBestVoPageResult.setItems(todayBestVoList);
        return todayBestVoPageResult;
    }


    //构造默认数据
    private List<RecommendUser> defaultRecommend() {
        String ids = "2,3,4,5,6,7,8,9,10,11";
        List<RecommendUser> records = new ArrayList<>();
        for (String id : ids.split(",")) {
            RecommendUser recommendUser = new RecommendUser();
            recommendUser.setUserId(Long.valueOf(id));
            recommendUser.setScore(RandomUtils.nextDouble(70, 98));
            records.add(recommendUser);
        }
        return records;
    }

    /**
     * 佳人信息
     */
    public TodayBestVo personalInfo(Long personUserId) {
        Long currentUserId = UserHolder.getUserId();
        //1.定义返回TodayBestVo
        TodayBestVo vo  = new TodayBestVo();
        //2.根据佳人用户id 查询佳人信息
        UserInfo userInfo = userInfoApi.queryUserInfo(personUserId);
        BeanUtils.copyProperties(userInfo,vo);
        if(!StringUtils.isEmpty(userInfo.getTags())){
            vo.setTags(userInfo.getTags().split(","));
        }
        //根据佳人用户id 与 推荐的用户id 查询缘分值
        RecommendUser recommendUser = recommendUserApi.queryScoreByPersonUserId(personUserId,currentUserId);
        //假设recommendUser为空 设置默认用户
        if(recommendUser == null){
            recommendUser = new RecommendUser();
            recommendUser.setScore(88d);
        }
        //设置缘分值
        vo.setFateValue(recommendUser.getScore().longValue());
        return vo;
    }

    /**
     * 查询陌生人问题
     */
    public String strangerQuestions(Long userId) {
        Question question = questionApi.queryByUserId(userId);
        String txt = "约吗?";//返回vo内容
        if(question != null && !StringUtils.isEmpty(question.getTxt())){
            txt = question.getTxt();
        }
        return txt;
    }

    /**
     * 回复陌生人问题
     */
    public void replyQuestion(Long personUserId, String reply) {

        //a.userId 通过UserHolder.getUserId()
        Long currentUserId = UserHolder.getUserId();
        //b.根据当前用户id查询userInfo得到昵称
        UserInfo userInfo = userInfoApi.queryUserInfo(currentUserId);
        String nickname = userInfo.getNickname();//当前用户昵称
        //c.根据传入的佳人用户id 查询tb_question 得到佳人问题
        Question question = questionApi.queryByUserId(personUserId);
        String txt = "约吗?";//返回vo内容
        if(question != null && !StringUtils.isEmpty(question.getTxt())){
            txt = question.getTxt();
        }
        //d.reply:回复的内容（传入的参数）
        //f.将以上数据map（json格式）调用环信云sendMsg(接收消息的用户id,msg消息内容)
        Map map = new HashMap();
        map.put("userId",currentUserId.toString());
        map.put("nickname",nickname);
        map.put("strangerQuestion",txt);
        map.put("reply",reply);
        String msg = JSON.toJSONString(map);
        huanXinTemplate.sendMsg(personUserId.toString(),msg);
    }
}
