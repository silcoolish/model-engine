package com.ljw.playdough.modelengine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_model")
public class SysModel extends BaseEntity {

    /**
     * 模型名称
     */
    private String name;

    /**
     * 模型唯一标识
     */
    private String code;

    /**
     * 物理表名
     */
    private String tableName;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态
     */
    private String status;

    /**
     * 版本号
     */
    private Integer version;


}