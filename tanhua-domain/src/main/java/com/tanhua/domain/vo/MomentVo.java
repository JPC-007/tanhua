package com.tanhua.domain.vo;
import lombok.Data;
import java.io.Serializable;
@Data
public class MomentVo implements Serializable {
    private String id; //动态id

    private Long userId; //用户id
    private String avatar; //头像
    private String nickname; //昵称
    private String gender; //性别 man woman
    private Integer age; //年龄
    private String[] tags; //标签


    private String textContent; //文字动态
    private String[] imageContent; //图片动态
    private String distance; //距离
    private String createDate; //发布时间 如: 10分钟前
    private int likeCount; //点赞数
    private int commentCount; //评论数
    private int loveCount; //喜欢数


    private Integer hasLiked; //是否点赞（1是，0否）
    private Integer hasLoved; //是否喜欢（1是，0否）
}