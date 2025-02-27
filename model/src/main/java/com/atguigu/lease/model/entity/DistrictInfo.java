package com.atguigu.lease.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@EqualsAndHashCode(callSuper = true)
@Schema(description = "地区信息表")
@TableName(value = "district_info")
@Data
public class DistrictInfo extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "区域名称")
    @TableField(value = "name")
    private String name;

    @Schema(description = "所属城市id")
    @TableField(value = "city_id")
    private Integer cityId;
}
