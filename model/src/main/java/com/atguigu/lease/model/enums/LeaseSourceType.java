package com.atguigu.lease.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum LeaseSourceType implements BaseEnum {
    NEW(1, "新签"),
    RENEW(2, "续约");

    @JsonValue
    @EnumValue
    private final Integer code;

    private final String name;

    LeaseSourceType(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
}
