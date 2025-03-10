package com.atguigu.lease.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum ItemType implements BaseEnum {
    APARTMENT(1, "公寓"),
    ROOM(2, "房间");

    @EnumValue
    @JsonValue
    private final Integer code;

    private final String name;

    ItemType(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
}
