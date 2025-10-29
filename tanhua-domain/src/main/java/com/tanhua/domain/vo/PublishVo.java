package com.tanhua.domain.vo;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class PublishVo implements Serializable {

    private Long userId; // 用户id
    private String textContent; // 文本内容
    private String location; // 地理位置
    private String longitude; // 经度
    private String latitude; // 纬度
    private List<String> medias; // 图片url
}