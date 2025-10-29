package com.tanhua.server.service;

import com.alibaba.fastjson.JSON;
import com.tanhua.commons.exception.TanHuaException;
import com.tanhua.commons.templates.HuanXinTemplate;
import com.tanhua.domain.db.User;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.Friend;
import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.vo.*;
import com.tanhua.dubbo.api.db.UserInfoApi;
import com.tanhua.dubbo.api.mongo.FriendApi;
import com.tanhua.dubbo.api.mongo.UserLikeApi;
import com.tanhua.dubbo.api.mongo.VisitorsApi;
import com.tanhua.server.interceptor.UserHolder;
import com.tanhua.server.utils.GetAgeUtil;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 业务处理类
 */
@Service
public class UserInfoService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Reference
    private UserInfoApi userInfoApi;

    @Value("${tanhua.tokenKey}")
    private String tokenKey;

    @Reference
    private FriendApi friendApi;

    @Reference
    private UserLikeApi userLikeApi;

    @Reference
    private VisitorsApi visitorsApi;

    @Autowired
    private HuanXinTemplate huanXinTemplate;

    /**
     * 查询用户信息
     */
    public UserInfoVo queryUserInfo(Long userID, Long huanxinID) {
        //定义返回UserInfoVo
        UserInfoVo userInfoVo = new UserInfoVo();
        //a.判断Token是否存在(判断是否登录了)
        /*User user = userService.getUser(token);
        if(user == null){
            throw new TanHuaException(ErrorResult.loginFail());
        }
        Long userId = user.getId();*/
        Long userId = UserHolder.getUserId();
        //b.调用服务提供者：根据当前用户id查询tb_userInfo数据
        UserInfo userInfo = userInfoApi.queryUserInfo(userId);
        //c.得到UserInfo后将数据copy到UserInfoVO中
        BeanUtils.copyProperties(userInfo, userInfoVo);
        //单独设置年龄
        if (!StringUtils.isEmpty(userInfo.getAge())) {
            userInfoVo.setAge(String.valueOf(userInfo.getAge()));
        } else {
            //通过调用工具类 将生日转成年龄
            userInfoVo.setAge(String.valueOf(GetAgeUtil.getAge(userInfo.getBirthday())));
        }

        //d.返回UserInfoVo
        return userInfoVo;
    }

    /**
     * 更新用户信息
     */
    public void updateUserInfo(UserInfoVo userInfoVo) {
        //a.判断Token是否存在(判断是否登录了)
        /*User user = userService.getUser(token);
        if(user == null){
            throw new TanHuaException(ErrorResult.loginFail());
        }
        Long userId = user.getId();*/
        Long userId = UserHolder.getUserId();
        //b.调用服务提供者：根据当前用户id更新tb_userInfo数据
        UserInfo userInfo = new UserInfo();
        BeanUtils.copyProperties(userInfoVo, userInfo);
        if (!StringUtils.isEmpty(userInfoVo.getBirthday())) {
            //当生日不为空  更新年龄
            //c.如果更新了生日，需要同步更新年龄字段
            userInfo.setAge(GetAgeUtil.getAge(userInfoVo.getBirthday()));
        }
        //最后设置当前用户id
        userInfo.setId(userId);
        userInfoApi.editUserInfo(userInfo);
    }

    /**
     * 互相喜欢，喜欢，粉丝 - 统计
     */
    public CountsVo queryCounts() {
        Long currentUserId = UserHolder.getUserId();
        CountsVo countsVo = new CountsVo();
        //1.互相喜欢统计
        Long eachLoveCount = friendApi.eachLoveCount(currentUserId);
        countsVo.setEachLoveCount(eachLoveCount);
        //2喜欢统计
        Long loveCount = userLikeApi.loveCount(currentUserId);
        countsVo.setLoveCount(loveCount);
        //3粉丝统计
        Long fanCount = userLikeApi.fanCount(currentUserId);
        ;
        countsVo.setFanCount(fanCount);
        return countsVo;
    }

    /**
     * 互相喜欢，喜欢，粉丝 谁看过我 分页查询
     * type:
     * 1 互相关注
     * 2 我关注
     * 3 粉丝
     * 4 谁看过我
     */
    public PageResult<FriendVo> queryMyLikeByPage(int page, int pagesize, String type) {
        Long currentUserId = UserHolder.getUserId();
        PageResult<FriendVo> friendVoPageResult = new PageResult<>();
        //查询互相关注 我关注 粉丝 谁看过我返回的数据统一返回RecommendUser对象
        PageResult<RecommendUser> pageResult = new PageResult<>();
        List<FriendVo> listFriendVo = new ArrayList<>();
        if (type.equals("1")) {
            //1 互相关注
            pageResult = friendApi.queryMyLikeByPage(page, pagesize, currentUserId);
        } else if (type.equals("2")) {
            //2 我关注
            pageResult = userLikeApi.queryMyLikeByPage(page,pagesize, currentUserId, true);
        } else if (type.equals("3")) {
            //3 粉丝
            pageResult = userLikeApi.queryMyLikeByPage(page,pagesize, currentUserId, false);
        } else if (type.equals("4")) {
            //4 谁看过我
            pageResult = visitorsApi.queryMyLikeByPage(page,pagesize, currentUserId);
        }
        if(StringUtils.isEmpty(pageResult) || CollectionUtils.isEmpty(pageResult.getItems())){
            //前端没有处理的很好 返回空对象设置值
            friendVoPageResult = new PageResult<>(0l,10l,0l,1l,null);
            return friendVoPageResult;
        }
        for (RecommendUser recommendUser : pageResult.getItems()) {
            FriendVo friendVo = new FriendVo();
            Double score = recommendUser.getScore();//缘分值
            Long userId = recommendUser.getUserId();//根据userid查询userInfo
            UserInfo userInfo = userInfoApi.queryUserInfo(userId);
            BeanUtils.copyProperties(userInfo,friendVo);
            if(StringUtils.isEmpty(score.intValue())) {
                friendVo.setMatchRate(66);//单独设置缘分值
            }else {
                friendVo.setMatchRate(score.intValue());//单独设置缘分值
            }
            listFriendVo.add(friendVo);
        }
        BeanUtils.copyProperties(pageResult, friendVoPageResult);
        friendVoPageResult.setItems(listFriendVo);
        return friendVoPageResult;
    }

    /**
     * 粉丝 - 喜欢
     */
    public void fansLike(Long fansUserId) {
        Long currentUserId = UserHolder.getUserId();
        //1.根据登录用户id 和 粉丝id 删除userLike关注记录
        userLikeApi.delete(fansUserId,currentUserId);
        //2.根据登录用户id 和  粉丝id 记录双向关系 tanhua_users
        Friend friend= new Friend();
        friend.setUserId(currentUserId);
        friend.setFriendId(fansUserId);
        friendApi.saveFriend(friend);
        //3.调用环信云 makeFriends方法记录好友关系（好友必须注册）
        huanXinTemplate.makeFriends(currentUserId,fansUserId);
    }
}
