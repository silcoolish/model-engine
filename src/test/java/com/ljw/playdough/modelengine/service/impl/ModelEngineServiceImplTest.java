package com.ljw.playdough.modelengine.service.impl;

import com.ljw.playdough.modelengine.common.BusinessException;
import com.ljw.playdough.modelengine.ddl.DdlExecutor;
import com.ljw.playdough.modelengine.ddl.TableMetadataReader;
import com.ljw.playdough.modelengine.ddl.TableValidator;
import com.ljw.playdough.modelengine.entity.SysModel;
import com.ljw.playdough.modelengine.entity.SysModelField;
import com.ljw.playdough.modelengine.entity.enums.ModelStatus;
import com.ljw.playdough.modelengine.service.SysModelFieldService;
import com.ljw.playdough.modelengine.service.SysModelService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModelEngineServiceImplTest {

    @Mock private SysModelService modelService;
    @Mock private SysModelFieldService fieldService;
    @Mock private DdlExecutor ddlExecutor;
    @Mock private TableValidator tableValidator;
    @Mock private TableMetadataReader tableMetadataReader;

    @InjectMocks
    private ModelEngineServiceImpl modelEngineService;

    // ======================== createModelWithTable ========================

    @Test
    void createModelWithTable_success() {
        SysModel model = buildModel(null, "user_info", "t_user_info", ModelStatus.DRAFT.name());
        List<SysModelField> fields = List.of(buildField("用户名", "username", "STRING"));

        when(tableValidator.exists("t_user_info")).thenReturn(false);
        doAnswer(inv -> { ((SysModel) inv.getArgument(0)).setId(1L); return true; })
                .when(modelService).save(any(SysModel.class));
        when(fieldService.saveBatch(anyList())).thenReturn(true);

        modelEngineService.createModelWithTable(model, fields);

        verify(modelService).save(model);
        verify(fieldService).saveBatch(fields);
        verify(ddlExecutor).execute(argThat(sql -> sql.contains("CREATE TABLE t_user_info")));
    }

    @Test
    void createModelWithTable_tableAlreadyExists_throwsBusinessException() {
        SysModel model = buildModel(null, "user_info", "t_user_info", ModelStatus.DRAFT.name());
        when(tableValidator.exists("t_user_info")).thenReturn(true);

        assertThatThrownBy(() -> modelEngineService.createModelWithTable(model, List.of()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Table already exists");

        verify(modelService, never()).save(any());
    }

    // ======================== updateModelSchema ========================

    @Test
    void updateModelSchema_success_addsNewColumn() {
        SysModel model = buildModel(1L, "user_info", "t_user_info", ModelStatus.DRAFT.name());
        SysModelField newField = buildField("用户名", "username", "STRING");

        when(modelService.getById(1L)).thenReturn(model);
        when(tableValidator.exists("t_user_info")).thenReturn(true);
        when(tableMetadataReader.getColumns("t_user_info")).thenReturn(List.of());
        when(fieldService.remove(any())).thenReturn(true);
        when(fieldService.saveBatch(anyList())).thenReturn(true);

        modelEngineService.updateModelSchema(1L, List.of(newField));

        verify(ddlExecutor).execute(argThat(sql -> sql.contains("ADD COLUMN username")));
        verify(fieldService).remove(any());
        verify(fieldService).saveBatch(anyList());
    }

    @Test
    void updateModelSchema_noAlterNeeded_whenColumnTypeMatches() {
        SysModel model = buildModel(1L, "user_info", "t_user_info", ModelStatus.DRAFT.name());
        SysModelField newField = buildField("用户名", "username", "STRING");

        TableMetadataReader.ColumnInfo existing = new TableMetadataReader.ColumnInfo();
        existing.setName("username");
        existing.setType("VARCHAR(255)");

        when(modelService.getById(1L)).thenReturn(model);
        when(tableValidator.exists("t_user_info")).thenReturn(true);
        when(tableMetadataReader.getColumns("t_user_info")).thenReturn(List.of(existing));
        when(fieldService.remove(any())).thenReturn(true);
        when(fieldService.saveBatch(anyList())).thenReturn(true);

        modelEngineService.updateModelSchema(1L, List.of(newField));

        verify(ddlExecutor, never()).execute(anyString());
        verify(fieldService).saveBatch(anyList());
    }

    @Test
    void updateModelSchema_modelNotFound_throwsBusinessException() {
        when(modelService.getById(99L)).thenReturn(null);

        assertThatThrownBy(() -> modelEngineService.updateModelSchema(99L, List.of()))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo(404);
    }

    @Test
    void updateModelSchema_tableNotExists_throwsBusinessException() {
        SysModel model = buildModel(1L, "user_info", "t_user_info", ModelStatus.DRAFT.name());
        when(modelService.getById(1L)).thenReturn(model);
        when(tableValidator.exists("t_user_info")).thenReturn(false);

        assertThatThrownBy(() -> modelEngineService.updateModelSchema(1L, List.of()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Physical table not exists");
    }

    // ======================== publishModel ========================

    @Test
    void publishModel_success() {
        SysModel model = buildModel(1L, "user_info", "t_user_info", ModelStatus.DRAFT.name());
        when(modelService.getById(1L)).thenReturn(model);
        when(tableValidator.exists("t_user_info")).thenReturn(true);
        when(modelService.updateById(any())).thenReturn(true);

        modelEngineService.publishModel(1L);

        verify(modelService).updateById(argThat(m -> ModelStatus.PUBLISHED.name().equals(m.getStatus())));
    }

    @Test
    void publishModel_modelNotFound_throwsBusinessException() {
        when(modelService.getById(99L)).thenReturn(null);

        assertThatThrownBy(() -> modelEngineService.publishModel(99L))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo(404);
    }

    @Test
    void publishModel_notDraft_throwsBusinessException() {
        SysModel model = buildModel(1L, "user_info", "t_user_info", ModelStatus.PUBLISHED.name());
        when(modelService.getById(1L)).thenReturn(model);

        assertThatThrownBy(() -> modelEngineService.publishModel(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Only draft model can be published");
    }

    @Test
    void publishModel_tableNotExists_throwsBusinessException() {
        SysModel model = buildModel(1L, "user_info", "t_user_info", ModelStatus.DRAFT.name());
        when(modelService.getById(1L)).thenReturn(model);
        when(tableValidator.exists("t_user_info")).thenReturn(false);

        assertThatThrownBy(() -> modelEngineService.publishModel(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Physical table not exists");
    }

    // ======================== offlineModel ========================

    @Test
    void offlineModel_success() {
        SysModel model = buildModel(1L, "user_info", "t_user_info", ModelStatus.PUBLISHED.name());
        when(modelService.getById(1L)).thenReturn(model);
        when(modelService.updateById(any())).thenReturn(true);

        modelEngineService.offlineModel(1L);

        verify(modelService).updateById(argThat(m -> ModelStatus.OFFLINE.name().equals(m.getStatus())));
    }

    @Test
    void offlineModel_modelNotFound_throwsBusinessException() {
        when(modelService.getById(99L)).thenReturn(null);

        assertThatThrownBy(() -> modelEngineService.offlineModel(99L))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo(404);
    }

    @Test
    void offlineModel_notPublished_throwsBusinessException() {
        SysModel model = buildModel(1L, "user_info", "t_user_info", ModelStatus.DRAFT.name());
        when(modelService.getById(1L)).thenReturn(model);

        assertThatThrownBy(() -> modelEngineService.offlineModel(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Only published model can be offline");
    }

    // ======================== softDeleteModel ========================

    @Test
    void softDeleteModel_success() {
        SysModel model = buildModel(1L, "user_info", "t_user_info", ModelStatus.PUBLISHED.name());
        when(modelService.getById(1L)).thenReturn(model);
        when(modelService.updateById(any())).thenReturn(true);

        modelEngineService.softDeleteModel(1L);

        verify(modelService).updateById(argThat(m ->
                ModelStatus.DELETED.name().equals(m.getStatus())
                        && Integer.valueOf(1).equals(m.getDeleted())));
    }

    @Test
    void softDeleteModel_modelNotFound_throwsBusinessException() {
        when(modelService.getById(99L)).thenReturn(null);

        assertThatThrownBy(() -> modelEngineService.softDeleteModel(99L))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo(404);
    }

    // ======================== hardDeleteModel ========================

    @Test
    void hardDeleteModel_success() {
        SysModel model = buildModel(1L, "user_info", "t_user_info", ModelStatus.OFFLINE.name());
        when(modelService.getById(1L)).thenReturn(model);
        when(fieldService.remove(any())).thenReturn(true);
        when(modelService.removeById(anyLong())).thenReturn(true);

        modelEngineService.hardDeleteModel(1L);

        verify(ddlExecutor).execute("DROP TABLE IF EXISTS t_user_info");
        verify(fieldService).remove(any());
        verify(modelService).removeById(1L);
    }

    @Test
    void hardDeleteModel_modelNotFound_throwsBusinessException() {
        when(modelService.getById(99L)).thenReturn(null);

        assertThatThrownBy(() -> modelEngineService.hardDeleteModel(99L))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo(404);
    }

    @Test
    void hardDeleteModel_notOffline_throwsBusinessException() {
        SysModel model = buildModel(1L, "user_info", "t_user_info", ModelStatus.PUBLISHED.name());
        when(modelService.getById(1L)).thenReturn(model);

        assertThatThrownBy(() -> modelEngineService.hardDeleteModel(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Only offline model can be permanently deleted");
    }

    // ======================== helpers ========================

    private SysModel buildModel(Long id, String code, String tableName, String status) {
        SysModel model = new SysModel();
        model.setId(id);
        model.setCode(code);
        model.setTableName(tableName);
        model.setStatus(status);
        return model;
    }

    private SysModelField buildField(String name, String code, String fieldType) {
        SysModelField field = new SysModelField();
        field.setName(name);
        field.setCode(code);
        field.setFieldType(fieldType);
        return field;
    }
}
