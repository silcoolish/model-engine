package com.ljw.playdough.modelengine.controller;

import com.ljw.playdough.modelengine.common.Result;
import com.ljw.playdough.modelengine.dto.FieldRequest;
import com.ljw.playdough.modelengine.dto.ModelCreateRequest;
import com.ljw.playdough.modelengine.dto.ModelUpdateSchemaRequest;
import com.ljw.playdough.modelengine.entity.SysModel;
import com.ljw.playdough.modelengine.entity.SysModelField;
import com.ljw.playdough.modelengine.service.ModelEngineService;
import com.ljw.playdough.modelengine.service.SysModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "模型管理", description = "数据模型的创建、查询、Schema 变更及生命周期管理")
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

    @Operation(summary = "创建模型", description = "创建模型元数据并在数据库中生成对应的物理表")
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

    @Operation(summary = "查询所有模型", description = "返回系统中所有模型的元数据列表")
    @GetMapping
    public Result<List<SysModel>> listModels() {
        return Result.success(sysModelService.list());
    }

    @Operation(summary = "查询单个模型", description = "根据模型 ID 返回模型详情")
    @Parameter(name = "id", description = "模型 ID", required = true)
    @GetMapping("/{id}")
    public Result<SysModel> getModel(@PathVariable Long id) {
        return Result.success(sysModelService.getById(id));
    }

    @Operation(summary = "更新模型 Schema", description = "替换模型的字段定义并执行 ALTER TABLE 同步物理表结构（仅 DRAFT 状态可操作）")
    @Parameter(name = "id", description = "模型 ID", required = true)
    @PutMapping("/{id}/schema")
    public Result<Void> updateSchema(@PathVariable Long id,
                                     @RequestBody @Valid ModelUpdateSchemaRequest request) {
        List<SysModelField> newFields = request.getNewFields().stream()
                .map(this::toSysModelField)
                .toList();
        modelEngineService.updateModelSchema(id, newFields);
        return Result.success();
    }

    @Operation(summary = "发布模型", description = "将模型状态从 DRAFT 推进到 PUBLISHED")
    @Parameter(name = "id", description = "模型 ID", required = true)
    @PostMapping("/{id}/publish")
    public Result<Void> publishModel(@PathVariable Long id) {
        modelEngineService.publishModel(id);
        return Result.success();
    }

    @Operation(summary = "下线模型", description = "将模型状态从 PUBLISHED 推进到 OFFLINE")
    @Parameter(name = "id", description = "模型 ID", required = true)
    @PostMapping("/{id}/offline")
    public Result<Void> offlineModel(@PathVariable Long id) {
        modelEngineService.offlineModel(id);
        return Result.success();
    }

    @Operation(summary = "软删除模型", description = "逻辑删除模型（设置 deleted=1），不删除物理表")
    @Parameter(name = "id", description = "模型 ID", required = true)
    @DeleteMapping("/{id}")
    public Result<Void> softDeleteModel(@PathVariable Long id) {
        modelEngineService.softDeleteModel(id);
        return Result.success();
    }

    @Operation(summary = "硬删除模型", description = "彻底删除模型元数据并 DROP 物理表（仅 OFFLINE 状态可操作）")
    @Parameter(name = "id", description = "模型 ID", required = true)
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
