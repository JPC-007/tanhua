package com.tanhua.dubbo.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 自定义字段填充类
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入填充数据
     * @param metaObject
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        Date nowDate = new Date();
        setFieldValByName("created",nowDate,metaObject);
        setFieldValByName("updated",nowDate,metaObject);
    }

    /**
     * 更新填充数据
     * @param metaObject
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        setFieldValByName("updated",new Date(),metaObject);
    }
}
