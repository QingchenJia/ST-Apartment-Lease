package com.atguigu.lease.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum BaseStatus implements BaseEnum {
    ENABLE(1, "正常"),
    DISABLE(0, "禁用");

    @EnumValue
    @JsonValue
    private final Integer code;

    private final String name;

    BaseStatus(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
}
