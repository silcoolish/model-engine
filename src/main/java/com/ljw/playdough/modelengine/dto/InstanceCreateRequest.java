package com.ljw.playdough.modelengine.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class InstanceCreateRequest {

    @NotNull(message = "fields must not be null")
    private Map<String, Object> fields;
}
