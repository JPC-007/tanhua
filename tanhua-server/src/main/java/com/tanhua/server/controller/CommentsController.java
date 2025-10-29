package com.tanhua.server.controller;

/**
 * 评论控制层
 */

import com.tanhua.domain.vo.CommentVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.PublishVo;
import com.tanhua.server.service.CommentsService;
import com.tanhua.server.service.MovementsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 评论控制层
 */
@RestController
@RequestMapping("/comments")
@Slf4j //日志注解
public class CommentsController {

    @Autowired
    private CommentsService commentsService;

    /**
     * 动态评论列表
     * movementId:动态编号 根据动态编号（发布id） 查询评论列表数据
     */
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity queryCommentsByPage(String movementId, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pagesize) {
        PageResult<CommentVo> pageResult = commentsService.queryCommentsByPage(movementId,page,pagesize);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 发表动态评论
     */
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity saveComment(@RequestBody Map<String,String> params) {
        String publishId = params.get("movementId");//动态id
        String content = params.get("comment");//评论内容
        commentsService.saveComment(publishId,content);
        return ResponseEntity.ok(null);
    }


    /**
     * 评论点赞
     * id:评论id (被评论内容主键id)
     */
    @RequestMapping(value = "/{id}/like",method = RequestMethod.GET)
    public ResponseEntity like(@PathVariable("id") String commentId) {
        Long count = commentsService.like(commentId);
        return ResponseEntity.ok(count);
    }

    /**
     * 评论取消点赞
     * id:评论id (被评论内容主键id)
     */
    @RequestMapping(value = "/{id}/dislike",method = RequestMethod.GET)
    public ResponseEntity dislike(@PathVariable("id") String commentId) {
        Long count = commentsService.dislike(commentId);
        return ResponseEntity.ok(count);
    }
}
