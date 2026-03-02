package com.ljw.playdough.modelengine.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ModelUpdateSchemaRequest {

    @NotEmpty(message = "字段列表不能为空")
    @Valid
    private List<FieldRequest> newFields;
}
