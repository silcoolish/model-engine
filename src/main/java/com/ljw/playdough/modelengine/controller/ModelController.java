package com.ljw.playdough.modelengine.controller;

import com.ljw.playdough.modelengine.common.Result;
import com.ljw.playdough.modelengine.dto.FieldRequest;
import com.ljw.playdough.modelengine.dto.ModelCreateRequest;
import com.ljw.playdough.modelengine.dto.ModelUpdateSchemaRequest;
import com.ljw.playdough.modelengine.entity.SysModel;
import com.ljw.playdough.modelengine.entity.SysModelField;
import com.ljw.playdough.modelengine.service.ModelEngineService;
import com.ljw.playdough.modelengine.service.SysModelService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/models")
public class ModelController {

    private final ModelEngineService modelEngineService;
    private final SysModelService sysModelService;

    public ModelController(ModelEngineService modelEngineService,
                           SysModelService sysModelService) {
        this.modelEngineService = modelEngineService;
        this.sysModelService = sysModelService;
    }

    @PostMapping
    public Result<Long> createModel(@RequestBody @Valid ModelCreateRequest request) {
        SysModel model = new SysModel();
        model.setName(request.getName());
        model.setCode(request.getCode());
        model.setTableName(request.getTableName());
        model.setDescription(request.getDescription());

        List<SysModelField> fields = request.getFields().stream()
                .map(this::toSysModelField)
                .toList();

        modelEngineService.createModelWithTable(model, fields);
        return Result.success(model.getId());
    }

    @GetMapping
    public Result<List<SysModel>> listModels() {
        return Result.success(sysModelService.list());
    }

    @GetMapping("/{id}")
    public Result<SysModel> getModel(@PathVariable Long id) {
        return Result.success(sysModelService.getById(id));
    }

    @PutMapping("/{id}/schema")
    public Result<Void> updateSchema(@PathVariable Long id,
                                     @RequestBody @Valid ModelUpdateSchemaRequest request) {
        List<SysModelField> newFields = request.getNewFields().stream()
                .map(this::toSysModelField)
                .toList();
        modelEngineService.updateModelSchema(id, newFields);
        return Result.success();
    }

    @PostMapping("/{id}/publish")
    public Result<Void> publishModel(@PathVariable Long id) {
        modelEngineService.publishModel(id);
        return Result.success();
    }

    @PostMapping("/{id}/offline")
    public Result<Void> offlineModel(@PathVariable Long id) {
        modelEngineService.offlineModel(id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> softDeleteModel(@PathVariable Long id) {
        modelEngineService.softDeleteModel(id);
        return Result.success();
    }

    @DeleteMapping("/{id}/hard")
    public Result<Void> hardDeleteModel(@PathVariable Long id) {
        modelEngineService.hardDeleteModel(id);
        return Result.success();
    }

    private SysModelField toSysModelField(FieldRequest req) {
        SysModelField field = new SysModelField();
        field.setName(req.getName());
        field.setCode(req.getCode());
        field.setFieldType(req.getFieldType());
        field.setLength(req.getLength());
        field.setRequired(req.getRequired());
        field.setDefaultValue(req.getDefaultValue());
        return field;
    }
}
