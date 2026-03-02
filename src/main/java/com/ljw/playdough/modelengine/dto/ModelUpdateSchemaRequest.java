package com.ljw.playdough.modelengine.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Schema(description = "更新模型 Schema 请求")
@Data
public class ModelUpdateSchemaRequest {

    @Schema(description = "新的字段定义列表（全量替换）")
    @NotEmpty(message = "字段列表不能为空")
    @Valid
    private List<FieldRequest> newFields;
}
