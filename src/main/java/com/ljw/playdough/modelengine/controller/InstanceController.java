package com.ljw.playdough.modelengine.controller;

import com.ljw.playdough.modelengine.common.Result;
import com.ljw.playdough.modelengine.dto.InstanceCreateRequest;
import com.ljw.playdough.modelengine.dto.InstanceQueryRequest;
import com.ljw.playdough.modelengine.dto.InstanceUpdateRequest;
import com.ljw.playdough.modelengine.dto.PageResult;
import com.ljw.playdough.modelengine.service.InstanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "实例数据管理", description = "针对已发布模型的实例数据 CRUD 与分页查询")
@RestController
@RequestMapping("/api/models/{modelCode}/instances")
public class InstanceController {

    private final InstanceService instanceService;

    public InstanceController(InstanceService instanceService) {
        this.instanceService = instanceService;
    }

    @Operation(summary = "创建实例", description = "向指定模型对应的物理表中插入一条数据记录")
    @Parameter(name = "modelCode", description = "模型标识码", required = true)
    @PostMapping
    public Result<Long> create(@PathVariable String modelCode,
                               @RequestBody @Valid InstanceCreateRequest request) {
        Long id = instanceService.createInstance(modelCode, request.getFields());
        return Result.success(id);
    }

    @Operation(summary = "更新实例", description = "根据 ID 更新指定模型物理表中的数据记录")
    @Parameter(name = "modelCode", description = "模型标识码", required = true)
    @Parameter(name = "id", description = "实例 ID", required = true)
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable String modelCode,
                               @PathVariable Long id,
                               @RequestBody @Valid InstanceUpdateRequest request) {
        instanceService.updateInstance(modelCode, id, request.getFields());
        return Result.success();
    }

    @Operation(summary = "删除实例", description = "根据 ID 删除指定模型物理表中的数据记录")
    @Parameter(name = "modelCode", description = "模型标识码", required = true)
    @Parameter(name = "id", description = "实例 ID", required = true)
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String modelCode,
                               @PathVariable Long id) {
        instanceService.deleteInstance(modelCode, id);
        return Result.success();
    }

    @Operation(summary = "分页查询实例", description = "根据过滤条件分页查询指定模型的实例数据")
    @Parameter(name = "modelCode", description = "模型标识码", required = true)
    @PostMapping("/query")
    public Result<PageResult<Map<String, Object>>> query(@PathVariable String modelCode,
                                                         @RequestBody @Valid InstanceQueryRequest request) {
        PageResult<Map<String, Object>> result = instanceService.queryInstances(
                modelCode,
                request.getFilters(),
                request.getPage(),
                request.getPageSize()
        );
        return Result.success(result);
    }
}
