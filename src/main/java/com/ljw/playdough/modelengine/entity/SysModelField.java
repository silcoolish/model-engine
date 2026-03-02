package com.ljw.playdough.modelengine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_model_field")
public class SysModelField extends BaseEntity {

    /**
     * 关联模型ID
     */
    private Long modelId;

    /**
     * 字段名称
     */
    private String name;

    /**
     * 字段标识
     */
    private String code;

    /**
     * 字段类型
     */
    private String fieldType;

    /**
     * 字段长度
     */
    private Integer length;

    /**
     * 是否必填
     */
    private Boolean required;

    /**
     * 默认值
     */
    private String defaultValue;
}