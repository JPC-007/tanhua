package com.tanhua.domain.mongo;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * <p>
 * 时间线表，用于存储发布（或推荐）的数据，每一个用户一张表进行存储
 * </p>
 */
@Data
public class TimeLine implements Serializable {

    private ObjectId id;

    private Long userId; // 好友id
    private ObjectId publishId; //发布id

    private Long created; //发布的时间
}