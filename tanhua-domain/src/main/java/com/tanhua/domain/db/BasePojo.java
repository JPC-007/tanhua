package com.tanhua.domain.db;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;
/**
 * 抽取BasePojo
 */
@Data
public abstract class BasePojo implements Serializable {
    /**
     * 插入时填充字段
     */
    @TableField(fill = FieldFill.INSERT)
    private Date created;

    /**
     * 插入和更新时填充字段
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updated;
}