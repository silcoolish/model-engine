package com.ljw.playdough.modelengine.service;

import com.ljw.playdough.modelengine.dto.PageResult;

import java.util.Map;

public interface InstanceService {

    Long createInstance(String modelCode, Map<String, Object> fields);

    void updateInstance(String modelCode, Long instanceId, Map<String, Object> fields);

    void deleteInstance(String modelCode, Long instanceId);

    PageResult<Map<String, Object>> queryInstances(String modelCode, Map<String, Object> filters, int page, int pageSize);
}
