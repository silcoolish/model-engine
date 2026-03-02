package com.ljw.playdough.modelengine.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Schema(description = "创建实例请求")
@Data
public class InstanceCreateRequest {

    @Schema(description = "字段键值对，key 为字段 code，value 为字段值",
            example = "{\"username\": \"张三\", \"age\": 18}")
    @NotNull(message = "fields must not be null")
    private Map<String, Object> fields;
}
