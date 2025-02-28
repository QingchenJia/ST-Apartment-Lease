package com.atguigu.lease.web.admin.custom.converter;

import com.atguigu.lease.model.enums.BaseEnum;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.stereotype.Component;

@Component
public class StringToBaseEnumConverterFactory implements ConverterFactory<String, BaseEnum> {
    /**
     * 获取一个转换器，用于将字符串代码转换为指定的枚举类型
     * 此方法是泛型的，可以适用于任何继承自BaseEnum的枚举类型
     * 它通过反射获取枚举常量，并根据代码找到对应的枚举值
     * 如果找不到匹配的代码，则抛出IllegalArgumentException异常
     *
     * @param targetType 目标枚举类型的Class对象，用于获取枚举常量
     * @param <T>        泛型参数，表示任何继承自BaseEnum的枚举类型
     * @return 返回一个Converter实例，用于将字符串代码转换为指定的枚举类型
     * @throws IllegalArgumentException 如果给定的代码在枚举类型中没有对应的枚举值时抛出
     */
    @Override
    public <T extends BaseEnum> Converter<String, T> getConverter(Class<T> targetType) {
        return code -> {
            // 获取目标枚举类型的所有枚举常量
            T[] enumConstants = targetType.getEnumConstants();

            // 遍历枚举常量，寻找与给定代码匹配的枚举值
            for (T value : enumConstants) {
                if (value.getCode().toString().equals(code)) {
                    // 如果找到匹配的枚举值，返回该枚举值
                    return value;
                }
            }

            // 如果没有找到匹配的枚举值，抛出异常
            throw new IllegalArgumentException();
        };
    }
}
