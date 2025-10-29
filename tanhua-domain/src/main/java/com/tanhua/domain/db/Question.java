package com.tanhua.domain.db;
import lombok.Data;

/**
 * 问题表实体对象
 */
@Data
public class Question extends BasePojo {
    private Long id;
    private Long userId;
    //问题内容
    private String txt;
}