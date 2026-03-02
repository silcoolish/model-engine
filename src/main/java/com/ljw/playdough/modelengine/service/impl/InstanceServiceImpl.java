package com.ljw.playdough.modelengine.service.impl;

import com.ljw.playdough.modelengine.common.BusinessException;
import com.ljw.playdough.modelengine.dto.PageResult;
import com.ljw.playdough.modelengine.entity.SysModel;
import com.ljw.playdough.modelengine.entity.SysModelField;
import com.ljw.playdough.modelengine.entity.enums.ModelStatus;
import com.ljw.playdough.modelengine.service.InstanceService;
import com.ljw.playdough.modelengine.service.SysModelFieldService;
import com.ljw.playdough.modelengine.service.SysModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class InstanceServiceImpl implements InstanceService {

    private final SysModelService modelService;
    private final SysModelFieldService fieldService;
    private final JdbcTemplate jdbcTemplate;

    public InstanceServiceImpl(SysModelService modelService,
                               SysModelFieldService fieldService,
                               JdbcTemplate jdbcTemplate) {
        this.modelService = modelService;
        this.fieldService = fieldService;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Shared guard: resolve model + validate PUBLISHED status + return field list.
     */
    private GuardResult guard(String modelCode) {
        SysModel model = modelService.getByCode(modelCode);
        if (model == null) {
            throw new BusinessException(404, "Model not found");
        }
        if (!ModelStatus.PUBLISHED.name().equals(model.getStatus())) {
            throw new BusinessException(400, "Model must be in PUBLISHED status");
        }
        List<SysModelField> fields = fieldService.listByModelId(model.getId());
        Set<String> validCodes = fields.stream()
                .map(SysModelField::getCode)
                .collect(Collectors.toSet());
        return new GuardResult(model.getTableName(), fields, validCodes);
    }

    @Override
    public Long createInstance(String modelCode, Map<String, Object> fields) {
        GuardResult guard = guard(modelCode);

        for (String key : fields.keySet()) {
            if (!guard.validCodes.contains(key)) {
                throw new BusinessException(400, "Unknown field: " + key);
            }
        }

        for (SysModelField field : guard.fields) {
            if (Boolean.TRUE.equals(field.getRequired())) {
                Object val = fields.get(field.getCode());
                if (val == null) {
                    throw new BusinessException(400, "Required field missing: " + field.getCode());
                }
            }
        }

        List<String> columns = new ArrayList<>(fields.keySet());
        columns.addAll(List.of("create_by", "update_by", "create_time", "update_time", "deleted"));

        String colPart = columns.stream().collect(Collectors.joining(", "));
        String placeholders = columns.stream().map(c -> "?").collect(Collectors.joining(", "));
        String sql = "INSERT INTO " + guard.tableName + " (" + colPart + ") VALUES (" + placeholders + ")";

        List<Object> args = new ArrayList<>(fields.values());
        LocalDateTime now = LocalDateTime.now();
        args.addAll(List.of(1L, 1L, now, now, 0));

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < args.size(); i++) {
                ps.setObject(i + 1, args.get(i));
            }
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    @Override
    public void updateInstance(String modelCode, Long instanceId, Map<String, Object> fields) {
        GuardResult guard = guard(modelCode);

        checkInstanceExists(guard.tableName, instanceId);

        for (String key : fields.keySet()) {
            if (!guard.validCodes.contains(key)) {
                throw new BusinessException(400, "Unknown field: " + key);
            }
        }

        List<String> setCols = new ArrayList<>(fields.keySet());
        setCols.addAll(List.of("update_by", "update_time"));

        String setPart = setCols.stream().map(c -> c + "=?").collect(Collectors.joining(", "));
        String sql = "UPDATE " + guard.tableName + " SET " + setPart + " WHERE id=? AND deleted=0";

        List<Object> args = new ArrayList<>(fields.values());
        args.addAll(List.of(1L, LocalDateTime.now(), instanceId));

        jdbcTemplate.update(sql, args.toArray());
    }

    @Override
    public void deleteInstance(String modelCode, Long instanceId) {
        GuardResult guard = guard(modelCode);

        checkInstanceExists(guard.tableName, instanceId);

        String sql = "UPDATE " + guard.tableName + " SET deleted=1, update_by=?, update_time=? WHERE id=? AND deleted=0";
        jdbcTemplate.update(sql, 1L, LocalDateTime.now(), instanceId);
    }

    @Override
    public PageResult<Map<String, Object>> queryInstances(String modelCode,
                                                          Map<String, Object> filters,
                                                          int page,
                                                          int pageSize) {
        GuardResult guard = guard(modelCode);

        List<Object> args = new ArrayList<>();
        StringBuilder where = new StringBuilder("deleted=0");

        if (filters != null && !filters.isEmpty()) {
            for (String key : filters.keySet()) {
                if (!guard.validCodes.contains(key)) {
                    throw new BusinessException(400, "Unknown filter field: " + key);
                }
                where.append(" AND ").append(key).append("=?");
                args.add(filters.get(key));
            }
        }

        String countSql = "SELECT COUNT(*) FROM " + guard.tableName + " WHERE " + where;
        Long total = jdbcTemplate.queryForObject(countSql, Long.class, args.toArray());
        if (total == null || total == 0) {
            return PageResult.empty(page, pageSize);
        }

        int offset = (page - 1) * pageSize;
        String selectSql = "SELECT * FROM " + guard.tableName + " WHERE " + where
                + " ORDER BY id ASC LIMIT ? OFFSET ?";

        List<Object> selectArgs = new ArrayList<>(args);
        selectArgs.add(pageSize);
        selectArgs.add(offset);

        List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql, selectArgs.toArray());
        return PageResult.of(total, page, pageSize, list);
    }

    private void checkInstanceExists(String tableName, Long instanceId) {
        String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE id=? AND deleted=0";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, instanceId);
        if (count == null || count == 0) {
            throw new BusinessException(404, "Instance not found");
        }
    }

    private record GuardResult(String tableName, List<SysModelField> fields, Set<String> validCodes) {}
}
