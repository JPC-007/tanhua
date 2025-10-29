package com.tanhua.domain.mongo;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * <p>
 * 相册表，用于存储自己发布的数据，每一个用户一张表进行存储
 * </p>
 */
@Data
public class Album implements Serializable {

    private ObjectId id; //主键id
    private ObjectId publishId; //发布id
    private Long created; //发布时间
}