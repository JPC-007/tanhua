package com.tanhua.manage.controller;

import com.tanhua.manage.service.AnalysisService;
import com.tanhua.manage.vo.AnalysisSummaryVo;
import com.tanhua.manage.vo.AnalysisUsersVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 概要统计分析
 */
@RestController
@RequestMapping("/dashboard")
@Slf4j
public class AnalysisController {

    @Autowired
    private AnalysisService analysisService;

    /**
     * 概要统计分析
     */
    @GetMapping("/summary")
    public ResponseEntity summary() {
        AnalysisSummaryVo analysisSummaryVo = analysisService.summary();
        return ResponseEntity.ok(analysisSummaryVo);
    }

    @GetMapping("/users")
    public AnalysisUsersVo getUsers(@RequestParam(name = "sd") Long sd
            , @RequestParam("ed") Long ed
            , @RequestParam("type") Integer type) {
        return this.analysisService.queryAnalysisUsersVo(sd, ed, type);
    }
}
