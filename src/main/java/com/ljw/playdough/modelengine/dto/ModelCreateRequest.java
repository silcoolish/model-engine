package com.ljw.playdough.modelengine.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ModelCreateRequest {

    @NotBlank(message = "模型名称不能为空")
    private String name;

    @NotBlank(message = "模型标识不能为空")
    private String code;

    @NotBlank(message = "物理表名不能为空")
    private String tableName;

    private String description;

    @NotEmpty(message = "字段列表不能为空")
    @Valid
    private List<FieldRequest> fields;
}
