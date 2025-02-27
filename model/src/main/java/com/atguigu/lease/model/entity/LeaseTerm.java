package com.atguigu.lease.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * &#064;TableName  lease_term
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value = "lease_term")
@Data
@Schema(description = "租期信息")
public class LeaseTerm extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "租期月数")
    @TableField("month_count")
    private Integer monthCount;

    @Schema(description = "租期单位:月")
    @TableField("unit")
    private String unit;
}