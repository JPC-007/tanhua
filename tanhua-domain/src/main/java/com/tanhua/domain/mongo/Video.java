package com.tanhua.domain.mongo;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Data
@Document(collection = "video")
public class Video implements Serializable {
    private ObjectId id; //主键id
    private Long userId;//发布小视频的用户id
    private Long vid; // 处境id-没有用到

    private String text; //文字 
    private String picUrl; //视频封面文件，URL
    private String videoUrl; //视频文件，URL
    private Long created; //创建时间

    private Integer likeCount=0; //点赞数
    private Integer commentCount=0; //评论数
    private Integer loveCount=0; //喜欢数
}