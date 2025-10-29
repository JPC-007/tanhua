package com.tanhua.domain.db;
import lombok.Data;

/**
 * 通用设置表实体对象
 */
@Data
public class Settings extends BasePojo {
    private Long id;
    private Long userId;
    private Boolean likeNotification;
    private Boolean pinglunNotification;
    private Boolean gonggaoNotification;
}