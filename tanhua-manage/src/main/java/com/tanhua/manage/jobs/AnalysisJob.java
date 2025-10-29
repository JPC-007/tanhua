package com.tanhua.manage.jobs;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.manage.domain.AnalysisByDay;
import com.tanhua.manage.mapper.AnalysisByDayMapper;
import com.tanhua.manage.mapper.LogMapper;
import com.tanhua.manage.utils.ComputeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 定时统计概要信息任务类
 */
@Component
@Slf4j
public class AnalysisJob {

    @Autowired
    private LogMapper logMapper;

    @Autowired
    private AnalysisByDayMapper analysisByDayMapper;

    /**
     * analysisLog:每隔5秒钟时间运行一次
     * cron = "0/5 * * * * ?"
     */
    @Scheduled(cron = "0/5 * * * * ?")
    public void analysisLog(){
        log.debug("定时统计概要信息任务类运行了。。。");
        Date date = new Date();
        //获取今日时间
        String todayDate = DateUtil.formatDate(date);//yyyy-MM-dd
        //获取昨天时间
        String yesterDate = ComputeUtil.offsetDay(date, -1);
        //1.num_registered：新注册用户数
        //select count(*) from tb_log where log_time = '2021-07-15' and type = '0102'
        Integer numRegistered = logMapper.queryNumsByType(todayDate,"0102");

        //2.num_active：活跃用户数
        //select count(DISTINCT user_id) from tb_log where log_time = '2021-07-15'
        Integer numActive = logMapper.queryNumsByDate(todayDate);

        //3.num_login：登陆次数
        //select count(*) from tb_log where log_time = '2021-07-15' and type = '0101'
        Integer numLogin = logMapper.queryNumsByType(todayDate,"0101");

        //4.num_retention1d：次日留存用户数
        //select count(*) from tb_log where user_id in(
        // select user_id from tb_log where  log_time ='2021-07-14' and type = '0102')
        // and log_time = '2021-07-15'
        Integer numRetention1d = logMapper.queryRetention1d(yesterDate,"0102",todayDate);

        //5.根据record_date=当天时间查询概要统计分析表 记录是否存在
        QueryWrapper<AnalysisByDay> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("record_date",todayDate);
        AnalysisByDay analysisByDay = analysisByDayMapper.selectOne(queryWrapper);
        //如果为空 往概要统计分析表中 插入数据
        if(analysisByDay == null){
            analysisByDay = new AnalysisByDay();
            analysisByDay.setRecordDate(DateUtil.parse(todayDate,"yyyy-MM-dd"));//今日时间
            analysisByDay.setNumRegistered(numRegistered);//新注册用户数
            analysisByDay.setNumActive(numActive);//活跃用户数
            analysisByDay.setNumLogin(numLogin);//登录次数
            analysisByDay.setNumRetention1d(numRetention1d);//次日留存用户数
            analysisByDayMapper.insert(analysisByDay);
        }else{
            //如果不为空 根据record_date=当天时间 更新概要统计分析表
            analysisByDay.setRecordDate(date);//今日时间
            analysisByDay.setNumRegistered(numRegistered);//新注册用户数
            analysisByDay.setNumActive(numActive);//活跃用户数
            analysisByDay.setNumLogin(numLogin);//登录次数
            analysisByDay.setNumRetention1d(numRetention1d);//次日留存用户数
            analysisByDayMapper.updateById(analysisByDay);
        }
    }
}
