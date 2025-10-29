package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Comment;
import com.tanhua.domain.vo.PageResult;

/**
 * 评论服务接口
 */
public interface CommentApi {
    /**
     * 动态点赞 点赞数量+1 返回点赞总数
     * @param comment
     * @return
     */
    long saveComment(Comment comment);

    /**
     * 动态取消点赞 点赞数量-1 返回点赞总数
     * @param comment
     * @return
     */
    long removeComment(Comment comment);

    /**
     * 评论列表分页查询
     * @param publishId
     * @param page
     * @param pagesize
     * @return
     */
    PageResult<Comment> queryCommentsByPage(String publishId, int page, int pagesize);

    /**
     * 点赞 喜欢 评论 列表
     * 评论类型，1-点赞，2-评论，3-喜欢
     */
    PageResult<Comment> queryCommentPage(Integer page, Integer pagesize, int num, Long currentUserId);
}
