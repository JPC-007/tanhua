package com.tanhua.manage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tanhua.manage.domain.Log;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 日志持久层接口
 */
@Mapper
public interface LogMapper extends BaseMapper<Log> {
    /**
     * 根据今日时间 操作类型 查询次数
     * @param todayDate 今日时间
     * @param type 操作类型
     * @return
     */
    @Select("select count(*) from tb_log where log_time = #{todayDate} and type = #{type}")
    Integer queryNumsByType(@Param("todayDate") String todayDate,@Param("type") String type);

    /**
     * 活跃用户数
     * @param todayDate
     * @return
     */
    @Select("select count(DISTINCT user_id) from tb_log where log_time = #{todayDate}")
    Integer queryNumsByDate(@Param("todayDate") String todayDate);

    /**
     * 次日留存用户数
     * @return
     */
    @Select("select count(*) from tb_log where user_id in(select user_id from tb_log " +
            "where  log_time =#{yesterDate} and type = #{type}) and log_time = #{todayDate}")
    Integer queryRetention1d(@Param("yesterDate") String yesterDate,@Param("type")  String type,@Param("todayDate")  String todayDate);
}
