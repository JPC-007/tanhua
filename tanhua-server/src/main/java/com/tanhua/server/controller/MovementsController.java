package com.tanhua.server.controller;

import com.tanhua.domain.db.User;
import com.tanhua.domain.vo.MomentVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.PublishVo;
import com.tanhua.domain.vo.VisitorVo;
import com.tanhua.server.service.MovementsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 圈子控制层
 */
@RestController
@RequestMapping("/movements")
@Slf4j //日志注解
public class MovementsController {

    @Autowired
    private MovementsService movementsService;

    /**
     * 发布动态
     */
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity savePublish(PublishVo publishVo, MultipartFile[] imageContent) {
        movementsService.savePublish(publishVo,imageContent);
        return ResponseEntity.ok(null);
    }


    /**
     * 好友动态
     */
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity queryPublishByTimeLine(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pagesize) {
        PageResult<MomentVo> pageResult = movementsService.queryPublishByTimeLine(page,pagesize);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 推荐动态
     */
    @RequestMapping(value = "/recommend",method = RequestMethod.GET)
    public ResponseEntity queryPublishByReQuanzi(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pagesize) {
        PageResult<MomentVo> pageResult = movementsService.queryPublishByReQuanzi(page,pagesize);
        return ResponseEntity.ok(pageResult);
    }


    /**
     *  用户动态（我的动态  推荐用户的动态）
     */
    @RequestMapping(value = "/all",method = RequestMethod.GET)
    public ResponseEntity queryPublishByAlbum(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pagesize,Long userId) {
        PageResult<MomentVo> pageResult = movementsService.queryPublishByAlbum(page,pagesize,userId);
        return ResponseEntity.ok(pageResult);
    }


    /**
     * 动态点赞
     */
    @RequestMapping(value = "/{id}/like",method = RequestMethod.GET)
    public ResponseEntity like(@PathVariable("id") String publishId) {
        //根据动态id 进行动态点赞并返回动态点赞数量
        Long count = movementsService.like(publishId);
        return ResponseEntity.ok(count);
    }

    /**
     * 动态取消点赞
     */
    @RequestMapping(value = "/{id}/dislike",method = RequestMethod.GET)
    public ResponseEntity dislike(@PathVariable("id") String publishId) {
        //根据动态id 进行动态取消点赞并返回动态点赞数量
        Long count = movementsService.dislike(publishId);
        return ResponseEntity.ok(count);
    }

    /**
     * 动态喜欢
     */
    @RequestMapping(value = "/{id}/love",method = RequestMethod.GET)
    public ResponseEntity love(@PathVariable("id") String publishId) {
        //根据动态id 进行动态喜欢并返回动态喜欢数量
        Long count = movementsService.love(publishId);
        return ResponseEntity.ok(count);
    }

    /**
     * 动态取消喜欢
     */
    @RequestMapping(value = "/{id}/unlove",method = RequestMethod.GET)
    public ResponseEntity unlove(@PathVariable("id") String publishId) {
        //根据动态id 进行动态取消喜欢并返回动态喜欢数量
        Long count = movementsService.unlove(publishId);
        return ResponseEntity.ok(count);
    }

    /**
     * 单条动态/movements/{id}
     */
    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    public ResponseEntity queryPublish(@PathVariable("id") String publishId) {
        MomentVo momentVo = movementsService.queryPublish(publishId);
        return ResponseEntity.ok(momentVo);
    }

    /**
     * 谁看过我
     */
    @RequestMapping(value = "/visitors",method = RequestMethod.GET)
    public ResponseEntity queryVisitors() {
        List<VisitorVo> visitorVoList = movementsService.queryVisitors();
        return ResponseEntity.ok(visitorVoList);
    }

}
