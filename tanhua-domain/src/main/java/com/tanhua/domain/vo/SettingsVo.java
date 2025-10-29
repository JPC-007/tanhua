package com.tanhua.domain.vo;

import lombok.Data;

/**
 * 通用设置Vo
 */
@Data
public class SettingsVo {
    private Long id;
    private String strangerQuestion;
    private String phone;
    private Boolean likeNotification;
    private Boolean pinglunNotification;
    private Boolean gonggaoNotification;
}