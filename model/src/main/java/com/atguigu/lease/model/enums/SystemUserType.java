package com.atguigu.lease.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum SystemUserType implements BaseEnum {
    ADMIN(0, "管理员"),
    COMMON(1, "普通用户");

    @EnumValue
    @JsonValue
    private final Integer code;

    private final String name;

    SystemUserType(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
}
