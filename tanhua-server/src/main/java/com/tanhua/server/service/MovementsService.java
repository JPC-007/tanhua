package com.tanhua.server.service;

import com.tanhua.commons.exception.TanHuaException;
import com.tanhua.commons.templates.OssTemplate;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.Comment;
import com.tanhua.domain.mongo.Publish;
import com.tanhua.domain.mongo.Visitor;
import com.tanhua.domain.vo.*;
import com.tanhua.dubbo.api.db.UserInfoApi;
import com.tanhua.dubbo.api.mongo.CommentApi;
import com.tanhua.dubbo.api.mongo.PublishApi;
import com.tanhua.dubbo.api.mongo.VisitorsApi;
import com.tanhua.server.interceptor.UserHolder;
import com.tanhua.server.utils.RelativeDateFormat;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 圈子业务处理层
 */
@Service
@Slf4j
public class MovementsService {

    @Autowired
    private OssTemplate ossTemplate;

    @Reference
    private PublishApi publishApi;

    @Reference
    private UserInfoApi userInfoApi;

    @Reference
    private CommentApi commentApi;

    @Reference
    private VisitorsApi visitorsApi;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 发布动态
     */
    public void savePublish(PublishVo publishVo, MultipartFile[] imageContent) {
        try {
            //1.imageContent处理-上传oss 并封装成List<String>
            List<String> medias = new ArrayList<>();
            if(imageContent != null && imageContent.length>0){
                for (MultipartFile multipartFile : imageContent) {
                    String imgUrl = ossTemplate.upload(multipartFile.getOriginalFilename(), multipartFile.getInputStream());
                    medias.add(imgUrl);
                }
            }
            //设置到PublishVo中
            publishVo.setMedias(medias);
            //设置当前的用户id
            publishVo.setUserId(UserHolder.getUserId());
            //2.调用服务提供者-发布动态
            String publishId = publishApi.savePublish(publishVo);
            //3.发布动态成功后，将发布id写入消息队列中
            rocketMQTemplate.convertAndSend("tanhua-publish",publishId);
        } catch (IOException e) {
            throw new TanHuaException(ErrorResult.error());
        }
    }

    /**
     * 好友动态
     */
    public PageResult<MomentVo> queryPublishByTimeLine(int page, int pagesize) {
        //定义返回的PageResult<MomentVo>
        PageResult<MomentVo> voPageResult = new PageResult<>();
        Long currentUserId = UserHolder.getUserId();//当前用户id
        //a.分页查询自己的时间表服务（好友时间线表）
        PageResult<Publish> pageResult = publishApi.queryPublishByTimeLine(page,pagesize,currentUserId);
        if(StringUtils.isEmpty(pageResult) || CollectionUtils.isEmpty(pageResult.getItems())){
            //前端没有处理的很好 返回空对象设置值
            voPageResult = new PageResult<>(0l,10l,0l,1l,null);
            return voPageResult;
        }
        //将List<Publish> 与 UserInfo 转为 List<MomentVo>
        List<MomentVo> momentVoList = new ArrayList<>();
        //b.再根据发布表中发布动态的用户id 查询用户信息
        for (Publish publish : pageResult.getItems()) {
            MomentVo momentVo = new MomentVo();

            Long userId = publish.getUserId();//动态发布的用户id
            UserInfo userInfo = userInfoApi.queryUserInfo(userId);

            //将UserInfo对象数据 copy momentVo
            BeanUtils.copyProperties(userInfo,momentVo);
            //将Publish对象数据 copy momentVo
            BeanUtils.copyProperties(publish,momentVo);

            if(!StringUtils.isEmpty(userInfo.getTags())) {
                momentVo.setTags(userInfo.getTags().split(","));
            }
            momentVo.setId(publish.getId().toHexString());///动态id
            //将list集合转为string数组
            momentVo.setImageContent(publish.getMedias().toArray(new String[]{}));
            //距离 写死了
            momentVo.setDistance("1米");
            //将发布表中时间 转为 几个小时前发布的
            momentVo.setCreateDate(RelativeDateFormat.format(new Date(publish.getCreated())));

            String key = "publish_like_"+currentUserId+"_"+publish.getId().toHexString();
            if(StringUtils.isEmpty(redisTemplate.opsForValue().get(key))){
                momentVo.setHasLiked(0);////是否点赞（1是，0否）
            }
            else
            {
                momentVo.setHasLiked(1);////是否点赞（1是，0否）
            }

            /*String key2 = "publish_love_"+currentUserId+"_"+publish.getId().toHexString();
            if(StringUtils.isEmpty(redisTemplate.opsForValue().get(key2))){
                momentVo.setHasLoved(0);////是否喜欢（1是，0否）
            }
            else
            {
                momentVo.setHasLoved(1);////是否喜欢（1是，0否）
            }*/
            momentVoList.add(momentVo);
        }
        //d.将发布表数据 跟 用户数据 封装Vo返回
        BeanUtils.copyProperties(pageResult,voPageResult);//分页数据
        voPageResult.setItems(momentVoList);//设置vo list集合数据
        return voPageResult;
    }

    /**
     * 推荐动态
     */
    public PageResult<MomentVo> queryPublishByReQuanzi(int page, int pagesize) {
        //定义返回的PageResult<MomentVo>
        PageResult<MomentVo> voPageResult = new PageResult<>();
        Long currentUserId = UserHolder.getUserId();//当前用户id
        //a.分页查询自己的时间表服务（好友时间线表）
        PageResult<Publish> pageResult = publishApi.queryPublishByReQuanzi(page,pagesize,currentUserId);
        if(StringUtils.isEmpty(pageResult) || CollectionUtils.isEmpty(pageResult.getItems())){
            //前端没有处理的很好 返回空对象设置值
            voPageResult = new PageResult<>(0l,10l,0l,1l,null);
            return voPageResult;
        }
        //将List<Publish> 与 UserInfo 转为 List<MomentVo>
        List<MomentVo> momentVoList = new ArrayList<>();
        //b.再根据发布表中发布动态的用户id 查询用户信息
        for (Publish publish : pageResult.getItems()) {
            MomentVo momentVo = new MomentVo();

            Long userId = publish.getUserId();//动态发布的用户id
            UserInfo userInfo = userInfoApi.queryUserInfo(userId);

            //将UserInfo对象数据 copy momentVo
            BeanUtils.copyProperties(userInfo,momentVo);
            //将Publish对象数据 copy momentVo
            BeanUtils.copyProperties(publish,momentVo);

            if(!StringUtils.isEmpty(userInfo.getTags())) {
                momentVo.setTags(userInfo.getTags().split(","));
            }
            momentVo.setId(publish.getId().toHexString());///动态id
            //将list集合转为string数组
            momentVo.setImageContent(publish.getMedias().toArray(new String[]{}));
            //距离 写死了
            momentVo.setDistance("1米");
            //将发布表中时间 转为 几个小时前发布的
            momentVo.setCreateDate(RelativeDateFormat.format(new Date(publish.getCreated())));
            String key = "publish_like_"+currentUserId+"_"+publish.getId().toHexString();
            if(StringUtils.isEmpty(redisTemplate.opsForValue().get(key))){
                momentVo.setHasLiked(0);////是否点赞（1是，0否）
            }
            else
            {
                momentVo.setHasLiked(1);////是否点赞（1是，0否）
            }
            String key2 = "publish_love_"+currentUserId+"_"+publish.getId().toHexString();
            if(StringUtils.isEmpty(redisTemplate.opsForValue().get(key2))){
                momentVo.setHasLoved(0);////是否喜欢（1是，0否）
            }
            else
            {
                momentVo.setHasLoved(1);////是否喜欢（1是，0否）
            }
            momentVoList.add(momentVo);
        }
        //d.将发布表数据 跟 用户数据 封装Vo返回
        BeanUtils.copyProperties(pageResult,voPageResult);//分页数据
        voPageResult.setItems(momentVoList);//设置vo list集合数据
        return voPageResult;
    }

    /**
     *  用户动态（我的动态  推荐用户的动态）
     *  userId:后续首页推荐用户动态功能会使用到
     */
    public PageResult<MomentVo> queryPublishByAlbum(int page, int pagesize, Long userId) {
        log.debug("用户动态查询参数{}：：：：{}：：：：{}：：：：",page,pagesize,userId);
        //定义返回的PageResult<MomentVo>
        PageResult<MomentVo> voPageResult = new PageResult<>();
        Long currentUserId = UserHolder.getUserId();//当前用户id
        //a.分页查询自己的时间表服务（好友时间线表）
        PageResult<Publish> pageResult = publishApi.queryPublishByAlbum(page,pagesize,userId);
        if(StringUtils.isEmpty(pageResult) || CollectionUtils.isEmpty(pageResult.getItems())){
            //前端没有处理的很好 返回空对象设置值
            voPageResult = new PageResult<>(0l,10l,0l,1l,null);
            return voPageResult;
        }
        //将List<Publish> 与 UserInfo 转为 List<MomentVo>
        List<MomentVo> momentVoList = new ArrayList<>();
        //b.再根据发布表中发布动态的用户id 查询用户信息
        for (Publish publish : pageResult.getItems()) {
            MomentVo momentVo = new MomentVo();

            Long publishUserId = publish.getUserId();//动态发布的用户id
            UserInfo userInfo = userInfoApi.queryUserInfo(publishUserId);

            //将UserInfo对象数据 copy momentVo
            BeanUtils.copyProperties(userInfo,momentVo);
            //将Publish对象数据 copy momentVo
            BeanUtils.copyProperties(publish,momentVo);

            if(!StringUtils.isEmpty(userInfo.getTags())) {
                momentVo.setTags(userInfo.getTags().split(","));
            }
            momentVo.setId(publish.getId().toHexString());///动态id
            //将list集合转为string数组
            momentVo.setImageContent(publish.getMedias().toArray(new String[]{}));
            //距离 写死了
            momentVo.setDistance("1米");
            //将发布表中时间 转为 几个小时前发布的
            momentVo.setCreateDate(RelativeDateFormat.format(new Date(publish.getCreated())));
            String key = "publish_like_"+currentUserId+"_"+publish.getId().toHexString();
            if(StringUtils.isEmpty(redisTemplate.opsForValue().get(key))){
                momentVo.setHasLiked(0);////是否点赞（1是，0否）
            }
            else
            {
                momentVo.setHasLiked(1);////是否点赞（1是，0否）
            }

            String key2 = "publish_love_"+currentUserId+"_"+publish.getId().toHexString();
            if(StringUtils.isEmpty(redisTemplate.opsForValue().get(key2))){
                momentVo.setHasLoved(0);////是否喜欢（1是，0否）
            }
            else
            {
                momentVo.setHasLoved(1);////是否喜欢（1是，0否）
            }

            momentVoList.add(momentVo);
        }
        //d.将发布表数据 跟 用户数据 封装Vo返回
        BeanUtils.copyProperties(pageResult,voPageResult);//分页数据
        voPageResult.setItems(momentVoList);//设置vo list集合数据
        return voPageResult;
    }

    /**
     * 动态点赞 返回点赞数量
     * @param publishId
     * @return
     */
    public Long like(String publishId) {
        Long currentUserId = UserHolder.getUserId();
        //封装评论对象
        Comment comment  = new Comment();
        //a.封装评论对象 调用服务提供者保存点赞记录
        comment.setPublishId(new ObjectId(publishId));//发布id
        comment.setCommentType(1);//评论类型，1-点赞，2-评论，3-喜欢
        comment.setPubType(1);//评论内容类型： 1-对动态操作 2-对视频操作 3-对评论操作
        comment.setUserId(currentUserId);//评论人 当前登录用户id
        long count = commentApi.saveComment(comment);
        //b.将点赞记录写入redis(后续查询动态从redis中查询是否点赞)
        String key = "publish_like_"+currentUserId+"_"+publishId;
        redisTemplate.opsForValue().set(key,publishId);
        return count;
    }

    /**
     * 动态取消点赞
     * @param publishId
     * @return
     */
    public Long dislike(String publishId) {
        Long currentUserId = UserHolder.getUserId();
        //封装评论对象
        Comment comment  = new Comment();
       ///a.封装评论对象 调用服务提供者取消点赞记录
        comment.setPublishId(new ObjectId(publishId));//发布id
        comment.setCommentType(1);//评论类型，1-点赞，2-评论，3-喜欢
        comment.setPubType(1);//评论内容类型： 1-对动态操作 2-对视频操作 3-对评论操作
        comment.setUserId(currentUserId);//评论人 当前登录用户id
        long count = commentApi.removeComment(comment);
        //b.将点赞记录从redis删除(后续查询动态从redis中查询是否点赞)
        String key = "publish_like_"+currentUserId+"_"+publishId;
        redisTemplate.delete(key);
        return count;
    }

    /**
     *动态喜欢
     * @param publishId
     * @return
     */
    public Long love(String publishId) {
        Long currentUserId = UserHolder.getUserId();
        //封装评论对象
        Comment comment  = new Comment();
        //a.封装评论对象 调用服务提供者保存喜欢记录
        comment.setPublishId(new ObjectId(publishId));//发布id
        comment.setCommentType(3);//评论类型，1-点赞，2-评论，3-喜欢
        comment.setPubType(1);//评论内容类型： 1-对动态操作 2-对视频操作 3-对评论操作
        comment.setUserId(currentUserId);//评论人 当前登录用户id
        long count = commentApi.saveComment(comment);
        //b.将点赞记录写入redis(后续查询动态从redis中查询是否点赞)
        String key = "publish_love_"+currentUserId+"_"+publishId;
        redisTemplate.opsForValue().set(key,publishId);
        return count;
    }

    /**
     * 动态取消喜欢
     * @param publishId
     * @return
     */
    public Long unlove(String publishId) {
        Long currentUserId = UserHolder.getUserId();
        //封装评论对象
        Comment comment  = new Comment();
        ///a.封装评论对象 调用服务提供者取消喜欢记录
        comment.setPublishId(new ObjectId(publishId));//发布id
        comment.setCommentType(3);//评论类型，1-点赞，2-评论，3-喜欢
        comment.setPubType(1);//评论内容类型： 1-对动态操作 2-对视频操作 3-对评论操作
        comment.setUserId(currentUserId);//评论人 当前登录用户id
        long count = commentApi.removeComment(comment);
        //b.将点赞记录从redis删除(后续查询动态从redis中查询是否点赞)
        String key = "publish_love_"+currentUserId+"_"+publishId;
        redisTemplate.delete(key);
        return count;
    }

    /**
     * 单条动态
     */
    public MomentVo queryPublish(String publishId) {
        MomentVo momentVo = new MomentVo();
        //a.调用服务提供者 根据发布id查询发布表获取动态数据
        Publish publish = publishApi.queryPublish(publishId);
        //b.根据发布表中发布用户id 查询用户信息表 得到用户信息
        Long publishUserId = publish.getUserId();//动态发布的用户id
        UserInfo userInfo = userInfoApi.queryUserInfo(publishUserId);
        //c.将动态数据与用户信息封装Vo返回
        //将UserInfo对象数据 copy momentVo
        BeanUtils.copyProperties(userInfo,momentVo);
        //将Publish对象数据 copy momentVo
        BeanUtils.copyProperties(publish,momentVo);

        if(!StringUtils.isEmpty(userInfo.getTags())) {
            momentVo.setTags(userInfo.getTags().split(","));
        }
        momentVo.setId(publish.getId().toHexString());///动态id
        //将list集合转为string数组
        momentVo.setImageContent(publish.getMedias().toArray(new String[]{}));
        //距离 写死了
        momentVo.setDistance("1米");
        //将发布表中时间 转为 几个小时前发布的
        momentVo.setCreateDate(RelativeDateFormat.format(new Date(publish.getCreated())));
        return momentVo;
    }

    /**
     * 谁看过我
     */
    public List<VisitorVo> queryVisitors() {
        List<VisitorVo> visitorVoList = new ArrayList<>();
        Long currentUserId = UserHolder.getUserId();
        //当前用户上次登录时间
        String key ="visitor_time_"+currentUserId;
        String lastTime = redisTemplate.opsForValue().get(key);
        //a.如果没有上次登录时间，根据当前登录用户id 查询访客记录（默认查询5条）
        List<Visitor> list = new ArrayList<>();
        log.debug("谁看过我************当前用户id:{}***************上次访问时间:{}",currentUserId,lastTime);
        if(StringUtils.isEmpty(lastTime)){
            list = visitorsApi.queryVisitorsByUserId(currentUserId);
        }
        else
        //b.如果有上次登录时间，根据当前登录用户id 与  date大于上次登录时间  查询访客记录（默认查询5条）
        {
            //将String转为long
            Long myLastTime  = Long.parseLong(lastTime);
            list = visitorsApi.queryVisitorsByUserIdLastTime(currentUserId,myLastTime);
        }
        ///c.根据visitorUserId:来访的用户id 查询userInfo
        if(list != null && list.size()>0){
            for (Visitor visitor : list) {
                VisitorVo visitorVo = new VisitorVo();

                Long visitorUserId = visitor.getVisitorUserId();//访客用户id
                UserInfo userInfo = userInfoApi.queryUserInfo(visitorUserId);

                BeanUtils.copyProperties(userInfo,visitorVo);
                if(!StringUtils.isEmpty(userInfo.getTags())) {
                    visitorVo.setTags(userInfo.getTags().split(","));
                }
                visitorVo.setFateValue(visitor.getScore().intValue());//缘分值
                visitorVoList.add(visitorVo);
            }
        }
        redisTemplate.opsForValue().set(key,System.currentTimeMillis()+"");//最新登录时间
        ///d.封装Vo返回
        return visitorVoList;

    }
}
