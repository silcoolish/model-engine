package com.ljw.playdough.modelengine.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "字段定义")
@Data
public class FieldRequest {

    @Schema(description = "字段名称", example = "用户名")
    @NotBlank(message = "字段名称不能为空")
    private String name;

    @Schema(description = "字段标识（英文，对应列名）", example = "username")
    @NotBlank(message = "字段标识不能为空")
    private String code;

    @Schema(description = "字段类型（STRING/TEXT/INTEGER/LONG/DECIMAL/BOOLEAN/DATE/DATETIME）", example = "STRING")
    @NotBlank(message = "字段类型不能为空")
    private String fieldType;

    @Schema(description = "字段长度（STRING 类型时有效）", example = "128")
    private Integer length;

    @Schema(description = "是否必填", example = "true")
    private Boolean required;

    @Schema(description = "默认值", example = "")
    private String defaultValue;
}
