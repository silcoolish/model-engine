package com.ljw.playdough.modelengine.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.Map;

@Data
public class InstanceQueryRequest {

    private Map<String, Object> filters;

    @Min(value = 1, message = "page must be >= 1")
    private int page = 1;

    @Min(value = 1, message = "pageSize must be >= 1")
    @Max(value = 100, message = "pageSize must be <= 100")
    private int pageSize = 20;
}
