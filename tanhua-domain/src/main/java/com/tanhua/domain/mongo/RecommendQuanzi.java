package com.tanhua.domain.mongo;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * <p>
 * 推荐动态
 * </p>
 */
@Data
@Document(collection = "recommend_quanzi")
public class RecommendQuanzi implements Serializable {

    @Id
    private ObjectId id; // 主键
    @Indexed
    private Long userId; // 推荐的用户id
    private Long pid;
    private ObjectId publishId; // 发布的动态的id
    @Indexed
    private Double score = 0d; // 推荐分数
    private Long created; // 日期
}