package com.ljw.playdough.modelengine.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.Map;

@Schema(description = "分页查询实例请求")
@Data
public class InstanceQueryRequest {

    @Schema(description = "过滤条件，key 为字段 code，value 为精确匹配值",
            example = "{\"username\": \"张三\"}")
    private Map<String, Object> filters;

    @Schema(description = "页码，从 1 开始", example = "1", defaultValue = "1")
    @Min(value = 1, message = "page must be >= 1")
    private int page = 1;

    @Schema(description = "每页条数，最大 100", example = "20", defaultValue = "20")
    @Min(value = 1, message = "pageSize must be >= 1")
    @Max(value = 100, message = "pageSize must be <= 100")
    private int pageSize = 20;
}
