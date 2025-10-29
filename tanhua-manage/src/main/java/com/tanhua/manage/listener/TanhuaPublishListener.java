package com.tanhua.manage.listener;

import com.tanhua.commons.templates.HuaWeiUGCTemplate;
import com.tanhua.domain.mongo.Publish;
import com.tanhua.dubbo.api.mongo.PublishApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 动态审核监听类 将app用户操作消息写入消息队列 动态审核
 */
@Component
@RocketMQMessageListener(
        topic = "tanhua-publish",consumerGroup="tanhuaPublish"
)
@Slf4j
public class TanhuaPublishListener implements RocketMQListener<String> {

    @Reference
    private PublishApi publishApi;

    @Autowired
    private HuaWeiUGCTemplate huaWeiUGCTemplate;
    
    
    /**
     * 审核业务逻辑处理
     * @param publishId 发布id
     */
    @Override
    public void onMessage(String publishId) {
        log.debug("动态审核开始了。。。。。。"+publishId);
        Integer state=2;// 状态0：待审核，1：已审核，2：已驳回
        //1在onMessage方法中，根据消息中发布id查询发布表，获取文本内容 与 图片地址urls
        Publish publish = publishApi.queryPublish(publishId);
        if(publish != null) {
            String textContent = publish.getTextContent();//动态文本内容
            List<String> imgUrls = publish.getMedias();//动态图片地址
            //2.分别调用华为云api进行审核，只要有一个审核return false，结果审核驳回，反之审核通过。
            boolean flag1 = huaWeiUGCTemplate.textContentCheck(textContent);
            //将List集合转为String数组
            boolean flag2 = huaWeiUGCTemplate.imageContentCheck(imgUrls.toArray(new String[]{}));
            if(flag1 && flag2){
                state = 1;
            }
            //3根据发布id修改状态值=1 或 2
            publishApi.updatePublishState(publishId,state);
            log.debug("动态审核结束了。。。。。。"+state);
        }
    }
}
