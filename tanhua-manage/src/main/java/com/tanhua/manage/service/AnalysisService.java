package com.tanhua.manage.service;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tanhua.manage.domain.AnalysisByDay;
import com.tanhua.manage.mapper.AnalysisByDayMapper;
import com.tanhua.manage.utils.ComputeUtil;
import com.tanhua.manage.vo.AnalysisSummaryVo;
import com.tanhua.manage.vo.AnalysisUsersVo;
import com.tanhua.manage.vo.DataPointVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 概要统计分析
 */
@Service
@Slf4j
public class AnalysisService extends ServiceImpl<AnalysisByDayMapper, AnalysisByDay> {

    /**
     * 概要统计分析
     */
    public AnalysisSummaryVo summary() {
        Date date = new Date();//date类型 今日日期
        String endDate = DateUtil.formatDate(date); //今日日期
        String yesterdayDate = ComputeUtil.offsetDay(date, -1);//昨天日期

        AnalysisSummaryVo analysisSummaryVo = new AnalysisSummaryVo();
        analysisSummaryVo.setCumulativeUsers(queryCumulativeUsers());  //累计用户数
        analysisSummaryVo.setActivePassMonth(queryUserCount(ComputeUtil.offsetDay(date,-30),endDate,"num_active"));  //过去30天活跃用户数
        analysisSummaryVo.setActivePassWeek(queryUserCount(ComputeUtil.offsetDay(date,-7),endDate,"num_active"));  //过去7天活跃用户

        Long num_registered = queryUserCount(endDate, endDate, "num_registered");
        analysisSummaryVo.setNewUsersToday(num_registered);  //今日新增用户数量
        analysisSummaryVo.setNewUsersTodayRate(ComputeUtil.computeRate(num_registered,queryUserCount(yesterdayDate, yesterdayDate, "num_registered")));  //今日新增用户涨跌率

        Long num_login = queryUserCount(endDate, endDate, "num_login");
        analysisSummaryVo.setLoginTimesToday(num_login);  //今日登录次数
        analysisSummaryVo.setLoginTimesTodayRate(ComputeUtil.computeRate(num_login,queryUserCount(yesterdayDate, yesterdayDate, "num_login")));  //今日登录次数涨跌率

        Long num_active = queryUserCount(endDate, endDate, "num_active");
        analysisSummaryVo.setActiveUsersToday(num_active);  //今日活跃用户数量
        analysisSummaryVo.setActiveUsersTodayRate(ComputeUtil.computeRate(num_active,queryUserCount(yesterdayDate, yesterdayDate, "num_active")));  //今日活跃用户涨跌率
        return analysisSummaryVo;
    }


    /**
     * 累计用户数
     *  select sum(num_registered) from tb_analysis_by_day
     */
    public Long queryCumulativeUsers(){
        QueryWrapper<AnalysisByDay> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("sum(num_registered) as numRegistered");
        AnalysisByDay analysisByDay = getOne(queryWrapper);
        return Long.parseLong(analysisByDay.getNumRegistered().toString());
    }

    /**
     * 统计分析公共方法
     */
    public Long queryUserCount(String startDate,String endDate,String column){
        QueryWrapper<AnalysisByDay> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("sum("+column+") as numRegistered");//select sum(num_active) from tb_analysis_by_day
        queryWrapper.ge("record_date",startDate); //record_date >='2021-06-12'
        queryWrapper.le("record_date",endDate);//record_date <='2021-07-12'
        AnalysisByDay analysisByDay = getOne(queryWrapper);
        return Long.parseLong(analysisByDay.getNumRegistered().toString());
    }


    /**
     * 新增、活跃用户、次日留存率
     */
    public AnalysisUsersVo queryAnalysisUsersVo(Long sd, Long ed, Integer type) {

        DateTime startDate = DateUtil.date(sd);

        DateTime endDate = DateUtil.date(ed);

        AnalysisUsersVo analysisUsersVo = new AnalysisUsersVo();

        //今年数据
        analysisUsersVo.setThisYear(this.queryDataPointVos(startDate, endDate, type));
        //去年数据
        analysisUsersVo.setLastYear(this.queryDataPointVos(
                DateUtil.offset(startDate, DateField.YEAR, -1),
                DateUtil.offset(endDate, DateField.YEAR, -1), type)
        );

        return analysisUsersVo;
    }

    private List<DataPointVo> queryDataPointVos(DateTime sd, DateTime ed, Integer type) {

        String startDate = sd.toDateStr();

        String endDate = ed.toDateStr();

        String column = null;
        switch (type) { //101 新增 102 活跃用户 103 次日留存率
            case 101:
                column = "num_registered";
                break;
            case 102:
                column = "num_active";
                break;
            case 103:
                column = "num_retention1d";
                break;
            default:
                column = "num_active";
                break;
        }

        List<AnalysisByDay> analysisByDayList = super.list(Wrappers.<AnalysisByDay>query()
                .select("record_date , " + column + " as num_active")
                .ge("record_date", startDate)
                .le("record_date", endDate));

        return analysisByDayList.stream()
                .map(analysisByDay -> new DataPointVo(DateUtil.date(analysisByDay.getRecordDate()).toDateStr(), analysisByDay.getNumActive().longValue()))
                .collect(Collectors.toList());
    }

}
