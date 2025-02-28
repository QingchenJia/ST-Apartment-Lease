package com.atguigu.lease.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum AppointmentStatus implements BaseEnum {
    WAITING(1, "待看房"),
    CANCELED(2, "已取消"),
    VIEWED(3, "已看房");

    @EnumValue
    @JsonValue
    private final Integer code;

    private final String name;

    AppointmentStatus(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
}
