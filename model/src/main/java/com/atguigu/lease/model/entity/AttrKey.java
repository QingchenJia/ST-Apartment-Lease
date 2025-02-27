package com.atguigu.lease.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@EqualsAndHashCode(callSuper = true)
@Schema(description = "房间基本属性表")
@TableName(value = "attr_key")
@Data
public class AttrKey extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "属性key")
    @TableField(value = "name")
    private String name;
}
