package com.atguigu.lease.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serial;

/**
 * &#064;TableName  room_lease_term
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value = "room_lease_term")
@Data
@Schema(description = "房间租期关系表")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoomLeaseTerm extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "房间id")
    @TableField("room_id")
    private Long roomId;

    @Schema(description = "租期id")
    @TableField("lease_term_id")
    private Long leaseTermId;
}
