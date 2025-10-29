package com.tanhua.server.controller;

import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.SettingsVo;
import com.tanhua.server.service.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 通用设置控制层
 */
@RestController
@RequestMapping("/users")
public class SettingsController {
    @Autowired
    private SettingsService settingsService;

    /**
     * 通用设置查询
     */
    @RequestMapping(value = "/settings",method = RequestMethod.GET)
    public ResponseEntity querySettings(){
        SettingsVo settingsVo = settingsService.querySettings();
        return ResponseEntity.ok(settingsVo);
    }

    /**
     * 通用设置修改
     */
    @RequestMapping(value = "/notifications/setting",method = RequestMethod.POST)
    public ResponseEntity updateOrSaveSettings(@RequestBody Map params){
        boolean likeNotification = (boolean)params.get("likeNotification");
        boolean pinglunNotification = (boolean)params.get("pinglunNotification");
        boolean gonggaoNotification = (boolean)params.get("gonggaoNotification");
        settingsService.updateOrSaveSettings(likeNotification,pinglunNotification,gonggaoNotification);
        return ResponseEntity.ok(null);
    }


    /**
     * 设置陌生人问题 -保存 更新
     */
    @RequestMapping(value = "/questions",method = RequestMethod.POST)
    public ResponseEntity updateOrSaveQuestion(@RequestBody Map<String,String> params){
        String txt = params.get("content");//修改后问题内容
        settingsService.updateOrSaveQuestion(txt);
        return ResponseEntity.ok(null);
    }


    /**
     * 黑名单 - 翻页列表
     */
    @RequestMapping(value = "/blacklist",method = RequestMethod.GET)
    public ResponseEntity queryBlacklist(@RequestParam(defaultValue = "1") int page,@RequestParam(defaultValue = "10") int pagesize){
        PageResult pageResult = settingsService.queryBlacklist(page,pagesize);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 黑名单 - 移除
     */
    @RequestMapping(value = "/blacklist/{uid}",method = RequestMethod.DELETE)
    public ResponseEntity delBlacklist(@PathVariable("uid") Long blackUserId){
        settingsService.delBlacklist(blackUserId);
        return ResponseEntity.ok(null);
    }
}
