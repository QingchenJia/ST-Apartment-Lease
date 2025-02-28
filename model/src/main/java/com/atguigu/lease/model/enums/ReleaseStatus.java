package com.atguigu.lease.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum ReleaseStatus implements BaseEnum {
    RELEASED(1, "已发布"),
    NOT_RELEASED(0, "未发布");

    @EnumValue
    @JsonValue
    private final Integer code;

    private final String name;

    ReleaseStatus(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
}
