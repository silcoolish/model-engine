package com.ljw.playdough.modelengine.controller;

import com.ljw.playdough.modelengine.common.Result;
import com.ljw.playdough.modelengine.dto.InstanceCreateRequest;
import com.ljw.playdough.modelengine.dto.InstanceQueryRequest;
import com.ljw.playdough.modelengine.dto.InstanceUpdateRequest;
import com.ljw.playdough.modelengine.dto.PageResult;
import com.ljw.playdough.modelengine.service.InstanceService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/models/{modelCode}/instances")
public class InstanceController {

    private final InstanceService instanceService;

    public InstanceController(InstanceService instanceService) {
        this.instanceService = instanceService;
    }

    @PostMapping
    public Result<Long> create(@PathVariable String modelCode,
                               @RequestBody @Valid InstanceCreateRequest request) {
        Long id = instanceService.createInstance(modelCode, request.getFields());
        return Result.success(id);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable String modelCode,
                               @PathVariable Long id,
                               @RequestBody @Valid InstanceUpdateRequest request) {
        instanceService.updateInstance(modelCode, id, request.getFields());
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String modelCode,
                               @PathVariable Long id) {
        instanceService.deleteInstance(modelCode, id);
        return Result.success();
    }

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
