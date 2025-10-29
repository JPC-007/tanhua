package com.tanhua.server.service;

import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.Comment;
import com.tanhua.domain.vo.CommentVo;
import com.tanhua.domain.vo.MomentVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.api.db.UserInfoApi;
import com.tanhua.dubbo.api.mongo.CommentApi;
import com.tanhua.server.interceptor.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 评论业务处理层
 */
@Service
@Slf4j
public class CommentsService {

    @Reference
    private CommentApi commentApi;

    @Reference
    private UserInfoApi userInfoApi;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    /**
     * 动态评论列表
     * movementId:动态编号 根据动态编号（发布id） 查询评论列表数据
     */
    public PageResult<CommentVo> queryCommentsByPage(String publishId, int page, int pagesize) {
        Long currentUserId = UserHolder.getUserId();
        //定义返回的PageResult<MomentVo>
        PageResult<CommentVo> voPageResult = new PageResult<>();
        //1.根据发布id 分页参数分页评论表数据
        PageResult<Comment> pageResult = commentApi.queryCommentsByPage(publishId,page,pagesize);
        if(StringUtils.isEmpty(pageResult) || CollectionUtils.isEmpty(pageResult.getItems())){
            //前端没有处理的很好 返回空对象设置值
            voPageResult = new PageResult<>(0l,10l,0l,1l,null);
            return voPageResult;
        }
        List<CommentVo> commentVoList = new ArrayList<>();
        //2.根据评论表中评论人userId查询UserInfo数据
        for (Comment comment : pageResult.getItems()) {
            CommentVo commentVo = new CommentVo();

            Long userId = comment.getUserId();//评论人用户id
            UserInfo userInfo = userInfoApi.queryUserInfo(userId);
            BeanUtils.copyProperties(userInfo,commentVo);//昵称 头像
            BeanUtils.copyProperties(comment,commentVo);//评论内容 点赞数
            commentVo.setCreateDate(new DateTime(comment.getCreated()).toString("yyyy年MM月dd日 HH:mm"));
            commentVo.setId(comment.getId().toHexString());//设置评论id

            String key2 = "comment_like_"+currentUserId+"_"+comment.getId().toHexString();
            if(StringUtils.isEmpty(redisTemplate.opsForValue().get(key2))){
                commentVo.setHasLiked(0);//是否点赞（1是，0否）
            }
            else
            {
                commentVo.setHasLiked(1);//是否点赞（1是，0否）
            }

            commentVoList.add(commentVo);
        }
        ///copy分页数据
        BeanUtils.copyProperties(pageResult,voPageResult);
        voPageResult.setItems(commentVoList);
        //3.将评论表数据与userInfo封装返回Vo
        return voPageResult;
    }

    /**
     * 发表动态评论
     */
    public void saveComment(String publishId, String content) {
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(publishId));//发布id
        comment.setCommentType(2);//评论类型，1-点赞，2-评论，3-喜欢
        comment.setPubType(1);//评论内容类型： 1-对动态操作 2-对视频操作 3-对评论操作
        comment.setContent(content);//评论内容
        comment.setUserId(UserHolder.getUserId());//当前登录用户id
        commentApi.saveComment(comment);
    }

    /**
     * 评论点赞
     * id:评论id (被评论内容主键id)
     */
    public Long like(String commentId) {
        Long currentUserId = UserHolder.getUserId();
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(commentId));//被点赞记录主键id
        comment.setCommentType(1);//评论类型，1-点赞，2-评论，3-喜欢
        comment.setPubType(3);//评论内容类型： 1-对动态操作 2-对视频操作 3-对评论操作
        comment.setUserId(currentUserId);//当前登录用户id
        //1.往评论表保存一条记录 2.更新评论表点赞数+1
        Long count = commentApi.saveComment(comment);
        //2.评论点赞记录写入redis中
        //b.将点赞记录写入redis(后续查询动态从redis中查询是否点赞)
        String key = "comment_like_"+currentUserId+"_"+commentId;
        redisTemplate.opsForValue().set(key,commentId);
        return count;
    }

    /**
     * 评论取消点赞
     * id:评论id (被评论内容主键id)
     */
    public Long dislike(String commentId) {
        Long currentUserId = UserHolder.getUserId();
        //封装评论对象
        Comment comment  = new Comment();
        ///a.封装评论对象 调用服务提供者取消喜欢记录
        comment.setPublishId(new ObjectId(commentId));//被评论的主键id
        comment.setCommentType(1);//评论类型，1-点赞，2-评论，3-喜欢
        comment.setPubType(3);//评论内容类型： 1-对动态操作 2-对视频操作 3-对评论操作
        comment.setUserId(currentUserId);//评论人 当前登录用户id
        long count = commentApi.removeComment(comment);
        //b.将点赞记录从redis删除(后续查询动态从redis中查询是否点赞)
        String key = "comment_like_"+currentUserId+"_"+commentId;
        redisTemplate.delete(key);
        return count;
    }
}
