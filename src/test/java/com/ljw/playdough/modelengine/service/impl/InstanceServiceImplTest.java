package com.ljw.playdough.modelengine.service.impl;

import com.ljw.playdough.modelengine.common.BusinessException;
import com.ljw.playdough.modelengine.dto.PageResult;
import com.ljw.playdough.modelengine.entity.SysModel;
import com.ljw.playdough.modelengine.entity.SysModelField;
import com.ljw.playdough.modelengine.entity.enums.ModelStatus;
import com.ljw.playdough.modelengine.service.SysModelFieldService;
import com.ljw.playdough.modelengine.service.SysModelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.KeyHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InstanceServiceImplTest {

    @Mock private SysModelService modelService;
    @Mock private SysModelFieldService fieldService;
    @Mock private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private InstanceServiceImpl instanceService;

    private SysModel publishedModel;
    private SysModelField usernameField;
    private SysModelField ageField;

    @BeforeEach
    void setUp() {
        publishedModel = new SysModel();
        publishedModel.setId(1L);
        publishedModel.setCode("user_info");
        publishedModel.setTableName("t_user_info");
        publishedModel.setStatus(ModelStatus.PUBLISHED.name());

        usernameField = new SysModelField();
        usernameField.setId(1L);
        usernameField.setModelId(1L);
        usernameField.setCode("username");
        usernameField.setName("用户名");
        usernameField.setRequired(false);

        ageField = new SysModelField();
        ageField.setId(2L);
        ageField.setModelId(1L);
        ageField.setCode("age");
        ageField.setName("年龄");
        ageField.setRequired(true);
    }

    // ======================== createInstance ========================

    @Test
    void createInstance_success_returnsGeneratedId() {
        when(modelService.getByCode("user_info")).thenReturn(publishedModel);
        when(fieldService.listByModelId(1L)).thenReturn(List.of(usernameField, ageField));
        doAnswer(inv -> {
            KeyHolder kh = inv.getArgument(1);
            kh.getKeyList().add(Map.of("id", 100L));
            return 1;
        }).when(jdbcTemplate).update(any(PreparedStatementCreator.class), any(KeyHolder.class));

        Map<String, Object> fields = new HashMap<>();
        fields.put("username", "张三");
        fields.put("age", 18);

        Long id = instanceService.createInstance("user_info", fields);

        assertThat(id).isEqualTo(100L);
        verify(jdbcTemplate).update(any(PreparedStatementCreator.class), any(KeyHolder.class));
    }

    @Test
    void createInstance_modelNotFound_throwsBusinessException() {
        when(modelService.getByCode("not_exist")).thenReturn(null);

        assertThatThrownBy(() -> instanceService.createInstance("not_exist", Map.of()))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo(404);
    }

    @Test
    void createInstance_modelNotPublished_throwsBusinessException() {
        SysModel draftModel = new SysModel();
        draftModel.setCode("user_info");
        draftModel.setStatus(ModelStatus.DRAFT.name());
        when(modelService.getByCode("user_info")).thenReturn(draftModel);

        assertThatThrownBy(() -> instanceService.createInstance("user_info", Map.of()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Model must be in PUBLISHED status");
    }

    @Test
    void createInstance_unknownField_throwsBusinessException() {
        when(modelService.getByCode("user_info")).thenReturn(publishedModel);
        when(fieldService.listByModelId(1L)).thenReturn(List.of(usernameField));

        assertThatThrownBy(() -> instanceService.createInstance("user_info", Map.of("bad_field", "v")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Unknown field: bad_field");
    }

    @Test
    void createInstance_requiredFieldMissing_throwsBusinessException() {
        when(modelService.getByCode("user_info")).thenReturn(publishedModel);
        when(fieldService.listByModelId(1L)).thenReturn(List.of(usernameField, ageField));

        // age is required but absent
        assertThatThrownBy(() -> instanceService.createInstance("user_info", Map.of("username", "张三")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Required field missing: age");
    }

    // ======================== updateInstance ========================

    @Test
    void updateInstance_success() {
        when(modelService.getByCode("user_info")).thenReturn(publishedModel);
        when(fieldService.listByModelId(1L)).thenReturn(List.of(usernameField));
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any())).thenReturn(1);

        instanceService.updateInstance("user_info", 1L, Map.of("username", "李四"));

        verify(jdbcTemplate).update(
                argThat(sql -> sql.contains("UPDATE t_user_info") && sql.contains("username=?")),
                any(Object[].class));
    }

    @Test
    void updateInstance_modelNotFound_throwsBusinessException() {
        when(modelService.getByCode("not_exist")).thenReturn(null);

        assertThatThrownBy(() -> instanceService.updateInstance("not_exist", 1L, Map.of()))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo(404);
    }

    @Test
    void updateInstance_instanceNotFound_throwsBusinessException() {
        when(modelService.getByCode("user_info")).thenReturn(publishedModel);
        when(fieldService.listByModelId(1L)).thenReturn(List.of(usernameField));
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any())).thenReturn(0);

        assertThatThrownBy(() -> instanceService.updateInstance("user_info", 99L, Map.of("username", "v")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Instance not found");
    }

    @Test
    void updateInstance_unknownField_throwsBusinessException() {
        when(modelService.getByCode("user_info")).thenReturn(publishedModel);
        when(fieldService.listByModelId(1L)).thenReturn(List.of(usernameField));
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any())).thenReturn(1);

        assertThatThrownBy(() -> instanceService.updateInstance("user_info", 1L, Map.of("no_such", "v")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Unknown field: no_such");
    }

    // ======================== deleteInstance ========================

    @Test
    void deleteInstance_success() {
        when(modelService.getByCode("user_info")).thenReturn(publishedModel);
        when(fieldService.listByModelId(1L)).thenReturn(List.of(usernameField));
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any())).thenReturn(1);

        instanceService.deleteInstance("user_info", 1L);

        verify(jdbcTemplate).update(
                argThat(sql -> sql.contains("deleted=1") && sql.contains("t_user_info")),
                any(), any(), any());
    }

    @Test
    void deleteInstance_instanceNotFound_throwsBusinessException() {
        when(modelService.getByCode("user_info")).thenReturn(publishedModel);
        when(fieldService.listByModelId(1L)).thenReturn(List.of(usernameField));
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any())).thenReturn(0);

        assertThatThrownBy(() -> instanceService.deleteInstance("user_info", 99L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Instance not found");
    }

    // ======================== queryInstances ========================

    @Test
    void queryInstances_returnsPageResult() {
        when(modelService.getByCode("user_info")).thenReturn(publishedModel);
        when(fieldService.listByModelId(1L)).thenReturn(List.of(usernameField));
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), any(Object[].class))).thenReturn(2L);
        when(jdbcTemplate.queryForList(anyString(), any(Object[].class)))
                .thenReturn(List.of(
                        Map.of("id", 1L, "username", "张三"),
                        Map.of("id", 2L, "username", "李四")));

        PageResult<Map<String, Object>> result =
                instanceService.queryInstances("user_info", null, 1, 20);

        assertThat(result.getTotal()).isEqualTo(2L);
        assertThat(result.getList()).hasSize(2);
        assertThat(result.getPage()).isEqualTo(1);
        assertThat(result.getPageSize()).isEqualTo(20);
    }

    @Test
    void queryInstances_emptyResult_whenTotalIsZero() {
        when(modelService.getByCode("user_info")).thenReturn(publishedModel);
        when(fieldService.listByModelId(1L)).thenReturn(List.of(usernameField));
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), any(Object[].class))).thenReturn(0L);

        PageResult<Map<String, Object>> result =
                instanceService.queryInstances("user_info", null, 1, 20);

        assertThat(result.getTotal()).isEqualTo(0L);
        assertThat(result.getList()).isEmpty();
        verify(jdbcTemplate, never()).queryForList(anyString(), any(Object[].class));
    }

    @Test
    void queryInstances_unknownFilterField_throwsBusinessException() {
        when(modelService.getByCode("user_info")).thenReturn(publishedModel);
        when(fieldService.listByModelId(1L)).thenReturn(List.of(usernameField));

        Map<String, Object> filters = Map.of("no_such_col", "val");

        assertThatThrownBy(() -> instanceService.queryInstances("user_info", filters, 1, 20))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Unknown filter field: no_such_col");
    }
}
