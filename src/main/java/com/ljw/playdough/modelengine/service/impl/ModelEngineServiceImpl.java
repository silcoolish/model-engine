package com.ljw.playdough.modelengine.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ljw.playdough.modelengine.common.BusinessException;
import com.ljw.playdough.modelengine.common.Result;
import com.ljw.playdough.modelengine.ddl.*;
import com.ljw.playdough.modelengine.ddl.DdlSecurityUtils;
import com.ljw.playdough.modelengine.entity.SysModel;
import com.ljw.playdough.modelengine.entity.SysModelField;
import com.ljw.playdough.modelengine.entity.enums.ModelStatus;
import com.ljw.playdough.modelengine.service.ModelEngineService;
import com.ljw.playdough.modelengine.service.SysModelFieldService;
import com.ljw.playdough.modelengine.service.SysModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class ModelEngineServiceImpl implements ModelEngineService {

    private final SysModelService modelService;
    private final SysModelFieldService fieldService;
    private final DdlExecutor ddlExecutor;
    private final TableValidator tableValidator;
    private final TableMetadataReader tableMetadataReader;

    public ModelEngineServiceImpl(
            SysModelService modelService,
            SysModelFieldService fieldService,
            DdlExecutor ddlExecutor,
            TableValidator tableValidator,
            TableMetadataReader tableMetadataReader) {
        this.modelService = modelService;
        this.fieldService = fieldService;
        this.ddlExecutor = ddlExecutor;
        this.tableValidator = tableValidator;
        this.tableMetadataReader = tableMetadataReader;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> createModelWithTable(SysModel model,
                                             List<SysModelField> fields) {

        log.info("开始创建模型: {}", model.getCode());

        if (tableValidator.exists(model.getTableName())) {
            throw new BusinessException(400, "Table already exists");
        }

        modelService.save(model);

        for (SysModelField field : fields) {
            field.setModelId(model.getId());
        }
        fieldService.saveBatch(fields);

        String sql = TableSqlGenerator.generateCreateTableSql(
                model.getTableName(),
                fields
        );

        ddlExecutor.execute(sql);

        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateModelSchema(Long modelId,
                                  List<SysModelField> newFields) {

        SysModel model = modelService.getById(modelId);

        if (model == null) {
            throw new BusinessException(404, "Model not found");
        }

        String tableName = model.getTableName();

        if (!tableValidator.exists(tableName)) {
            throw new BusinessException(400, "Physical table not exists");
        }

        // 1 获取数据库真实字段
        List<TableMetadataReader.ColumnInfo> dbColumns =
                tableMetadataReader.getColumns(tableName);

        // 2 生成差异 SQL
        List<String> alterSqlList =
                SchemaDiffEngine.generateAlterSql(
                        tableName,
                        newFields,
                        dbColumns
                );

        // 3 执行 ALTER
        for (String sql : alterSqlList) {
            ddlExecutor.execute(sql);
        }

        // 4 更新元数据
        fieldService.remove(
                new LambdaQueryWrapper<SysModelField>()
                        .eq(SysModelField::getModelId, modelId)
        );

        for (SysModelField field : newFields) {
            field.setModelId(modelId);
        }

        fieldService.saveBatch(newFields);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishModel(Long modelId) {

        SysModel model = modelService.getById(modelId);

        if (model == null) {
            throw new BusinessException(404, "Model not found");
        }

        if (!ModelStatus.DRAFT.name().equals(model.getStatus())) {
            throw new BusinessException(400, "Only draft model can be published");
        }

        if (!tableValidator.exists(model.getTableName())) {
            throw new BusinessException(400, "Physical table not exists");
        }

        model.setStatus(ModelStatus.PUBLISHED.name());
        modelService.updateById(model);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void offlineModel(Long modelId) {

        SysModel model = modelService.getById(modelId);

        if (model == null) {
            throw new BusinessException(404, "Model not found");
        }

        if (!ModelStatus.PUBLISHED.name().equals(model.getStatus())) {
            throw new BusinessException(400, "Only published model can be offline");
        }

        model.setStatus(ModelStatus.OFFLINE.name());
        modelService.updateById(model);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void softDeleteModel(Long modelId) {

        SysModel model = modelService.getById(modelId);

        if (model == null) {
            throw new BusinessException(404, "Model not found");
        }

        model.setDeleted(1);
        model.setStatus(ModelStatus.DELETED.name());

        modelService.updateById(model);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hardDeleteModel(Long modelId) {

        SysModel model = modelService.getById(modelId);

        if (model == null) {
            throw new BusinessException(404, "Model not found");
        }

        if (!ModelStatus.OFFLINE.name().equals(model.getStatus())) {
            throw new BusinessException(400,
                    "Only offline model can be permanently deleted");
        }

        // 1 删除物理表
        DdlSecurityUtils.validateIdentifier(model.getTableName(), "表名");
        ddlExecutor.execute("DROP TABLE IF EXISTS " + model.getTableName());

        // 2 删除字段元数据
        fieldService.remove(
                new LambdaQueryWrapper<SysModelField>()
                        .eq(SysModelField::getModelId, modelId)
        );

        // 3 删除模型元数据
        modelService.removeById(modelId);
    }

}