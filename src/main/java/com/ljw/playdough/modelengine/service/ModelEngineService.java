package com.ljw.playdough.modelengine.service;

import com.ljw.playdough.modelengine.common.Result;
import com.ljw.playdough.modelengine.entity.SysModel;
import com.ljw.playdough.modelengine.entity.SysModelField;

import java.util.List;

public interface ModelEngineService {

    /**
     * 创建数据模型并生成对应表
     *
     * @param model  模型
     * @param fields 字段列表
     */
    Result<Void> createModelWithTable(SysModel model, List<SysModelField> fields);

    /**
     * 数据模型字段更新
     *
     * @param modelId    模型ID
     * @param newFields 新字段列表
     */
    void updateModelSchema(Long modelId,
                           List<SysModelField> newFields);

    /**
     * 发布模型
     *
     * @param modelId 模型ID
     */
    void publishModel(Long modelId);

    /**
     * 下线模型
     *
     * @param modelId 模型ID
     */
    void offlineModel(Long modelId);

    /**
     * 软删除模型
     *
     * @param modelId 模型ID
     */
    void softDeleteModel(Long modelId);

    /**
     * 强制删除模型
     *
     * @param modelId 模型ID
     */
    void hardDeleteModel(Long modelId);
}