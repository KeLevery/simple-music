package com.simple.enumeration;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;
//用来存放枚举类型
@Getter
public enum UserStatusEnum {

    ENABLE(0, "启用"),
    DISABLE(1, "禁用");

    @EnumValue
    private final Integer id;
    private final String userStatus;

    UserStatusEnum(Integer id, String userStatus) {
        this.id = id;
        this.userStatus = userStatus;
    }

}
