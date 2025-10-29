package com.tanhua.server.controller;

import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.RecommendUserQueryParam;
import com.tanhua.domain.vo.TodayBestVo;
import com.tanhua.server.service.TodayBestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 佳人控制层
 */
@RestController
@RequestMapping("/tanhua")
public class TodayBestController {
    @Autowired
    private TodayBestService todayBestService;

    /**
     * 今日佳人
     */
    @RequestMapping(value = "/todayBest",method = RequestMethod.GET)
    public ResponseEntity todayBest(){
        TodayBestVo todayBestVo =todayBestService.todayBest();
        return ResponseEntity.ok(todayBestVo);
    }

    /**
     * 推荐用户列表（推荐佳人列表）
     */
    @RequestMapping(value = "/recommendation",method = RequestMethod.GET)
    public ResponseEntity recommendation(RecommendUserQueryParam recommendUserQueryParam){
        PageResult<TodayBestVo> pageResult =todayBestService.recommendation(recommendUserQueryParam);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 佳人信息
     */
    @RequestMapping(value = "/{id}/personalInfo",method = RequestMethod.GET)
    public ResponseEntity personalInfo(@PathVariable("id") Long id){
        TodayBestVo todayBestVo =todayBestService.personalInfo(id);
        return ResponseEntity.ok(todayBestVo);
    }

    /**
     * 查询陌生人问题
     */
    @RequestMapping(value = "/strangerQuestions",method = RequestMethod.GET)
    public ResponseEntity strangerQuestions(Long userId){
        String txt =todayBestService.strangerQuestions(userId);
        return ResponseEntity.ok(txt);
    }

    /**
     * 回复陌生人问题
     */
    @RequestMapping(value = "/strangerQuestions",method = RequestMethod.POST)
    public ResponseEntity replyQuestion(@RequestBody Map params){
        Long userId = Long.parseLong(params.get("userId").toString());
        String content = (String)params.get("reply");
        todayBestService.replyQuestion(userId,content);
        return ResponseEntity.ok(null);
    }
}
