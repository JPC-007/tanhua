package com.tanhua.server.controller;

import com.tanhua.domain.db.User;
import com.tanhua.domain.vo.ContactVo;
import com.tanhua.domain.vo.MessageVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.server.service.MessagesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 消息管理控制层
 */
@RestController
@RequestMapping("/messages")
@Slf4j //日志注解
public class MessagesController {

    @Autowired
    private MessagesService messagesService;
    /**
     * 添加联系人
     */
    @RequestMapping(value = "/contacts", method = RequestMethod.POST)
    public ResponseEntity saveContacts(@RequestBody Map params) {
        Long personUserId = Long.parseLong(params.get("userId").toString());
        messagesService.saveContacts(personUserId);
        return ResponseEntity.ok(null);
    }


    /**
     * 联系人列表
     * required = false:keyword 此参数非必须
     */
    @RequestMapping(value = "/contacts", method = RequestMethod.GET)
    public ResponseEntity queryContacts(@RequestParam(defaultValue = "1") Integer page,
                                        @RequestParam(defaultValue = "10") Integer pagesize,
                                        @RequestParam(required = false) String keyword) {
        PageResult<ContactVo> pageResult = messagesService.queryContacts(page,pagesize,keyword);
        return ResponseEntity.ok(pageResult);
    }


    /**
     * 喜欢列表
     * 评论类型，1-点赞，2-评论，3-喜欢
     */
    @RequestMapping(value = "/loves", method = RequestMethod.GET)
    public ResponseEntity loves(@RequestParam(defaultValue = "1") Integer page,
                                        @RequestParam(defaultValue = "10") Integer pagesize) {
        PageResult<MessageVo> pageResult = messagesService.queryCommentPage(page,pagesize,3);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 点赞列表
     * 评论类型，1-点赞，2-评论，3-喜欢
     */
    @RequestMapping(value = "/likes", method = RequestMethod.GET)
    public ResponseEntity likes(@RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "10") Integer pagesize) {
        PageResult<MessageVo> pageResult = messagesService.queryCommentPage(page,pagesize,1);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 评论列表
     * 评论类型，1-点赞，2-评论，3-喜欢
     */
    @RequestMapping(value = "/comments", method = RequestMethod.GET)
    public ResponseEntity comments(@RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "10") Integer pagesize) {
        PageResult<MessageVo> pageResult = messagesService.queryCommentPage(page,pagesize,2);
        return ResponseEntity.ok(pageResult);
    }
}
