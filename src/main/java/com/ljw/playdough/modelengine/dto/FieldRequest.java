package com.ljw.playdough.modelengine.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FieldRequest {

    @NotBlank(message = "字段名称不能为空")
    private String name;

    @NotBlank(message = "字段标识不能为空")
    private String code;

    @NotBlank(message = "字段类型不能为空")
    private String fieldType;

    private Integer length;

    private Boolean required;

    private String defaultValue;
}
