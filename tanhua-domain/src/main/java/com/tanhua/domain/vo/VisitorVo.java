package com.tanhua.domain.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class VisitorVo implements Serializable {
    private Long id;//访客用户id
    private String avatar;
    private String nickname;
    private String gender;
    private Integer age;
    private String[] tags;
    private Integer fateValue;
}