package com.ljw.playdough.modelengine.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Schema(description = "更新实例请求")
@Data
public class InstanceUpdateRequest {

    @Schema(description = "需要更新的字段键值对，key 为字段 code，value 为新值",
            example = "{\"username\": \"李四\"}")
    @NotNull(message = "fields must not be null")
    private Map<String, Object> fields;
}
