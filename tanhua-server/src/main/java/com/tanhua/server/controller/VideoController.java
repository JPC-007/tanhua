package com.tanhua.server.controller;

import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.VideoVo;
import com.tanhua.server.service.VideoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 小视频控制层
 */
@RestController
@RequestMapping("/smallVideos")
@Slf4j //日志注解
public class VideoController {

    @Autowired
    private VideoService videoService;

    /**
     * 发布小视频 smallVideos
     * videoThumbnail:视频封面
     * videoFile：视频文件
     */
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity saveSmallVideos(MultipartFile videoThumbnail,MultipartFile videoFile) {
        videoService.saveSmallVideos(videoThumbnail,videoFile);
        return ResponseEntity.ok(null);
    }

    /**
     * 小视频列表
     */
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity querySmallVideos(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pagesize) {
        page = page < 1 ? 1:page;
        PageResult<VideoVo> pageResult = videoService.querySmallVideos(page,pagesize);
        return ResponseEntity.ok(pageResult);
    }


    /**
     * 视频用户关注
     */
    @RequestMapping(value = "/{uid}/userFocus",method = RequestMethod.POST)
    public ResponseEntity userFocus(@PathVariable("uid") Long followUserId) {
        videoService.userFocus(followUserId);
        return ResponseEntity.ok(null);
    }

    /**
     * 视频用户关注
     */
    @RequestMapping(value = "/{uid}/userUnFocus",method = RequestMethod.POST)
    public ResponseEntity userUnFocus(@PathVariable("uid") Long followUserId) {
        videoService.userUnFocus(followUserId);
        return ResponseEntity.ok(null);
    }
}
