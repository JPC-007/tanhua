package com.tanhua.manage.listener;

import com.alibaba.fastjson.JSON;
import com.tanhua.manage.domain.Log;
import com.tanhua.manage.mapper.LogMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * 日志监听类 将app用户操作消息写入日志表中
 */
@Component
@RocketMQMessageListener(
        topic = "tanhua-logs",consumerGroup="tanhuaLogs"
)
@Slf4j
public class TanhuaLogMQListener implements RocketMQListener<String> {


    @Autowired
    private LogMapper logMapper;

    @Override
    public void onMessage(String message) {
        Map<String,String> map = JSON.parseObject(message, Map.class);
        String userId = map.get("userId");
        String logTime = map.get("logTime");
        String type = map.get("type");

        Log dbLog = new Log();
        dbLog.setUserId(Long.parseLong(userId));//设置用户id
        dbLog.setLogTime(logTime);//日志时间
        dbLog.setPlace("深圳");
        dbLog.setEquipment("华为P99");
        dbLog.setType(type);
        dbLog.setCreated(new Date());//创建日志时间
        logMapper.insert(dbLog);
        log.debug("日志保存成功了。。。。"+dbLog.toString());
    }
}
