package com.ljw.playdough.modelengine.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Schema(description = "创建模型请求")
@Data
public class ModelCreateRequest {

    @Schema(description = "模型名称", example = "用户信息")
    @NotBlank(message = "模型名称不能为空")
    private String name;

    @Schema(description = "模型标识（英文，唯一）", example = "user_info")
    @NotBlank(message = "模型标识不能为空")
    private String code;

    @Schema(description = "物理表名（英文，唯一）", example = "t_user_info")
    @NotBlank(message = "物理表名不能为空")
    private String tableName;

    @Schema(description = "模型描述", example = "存储用户基本信息")
    private String description;

    @Schema(description = "字段列表")
    @NotEmpty(message = "字段列表不能为空")
    @Valid
    private List<FieldRequest> fields;
}
