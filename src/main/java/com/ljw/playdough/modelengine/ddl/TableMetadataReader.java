package com.ljw.playdough.modelengine.ddl;

import lombok.Data;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TableMetadataReader {

    private final JdbcTemplate jdbcTemplate;

    public TableMetadataReader(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ColumnInfo> getColumns(String tableName) {

        String sql = """
                SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE
                FROM information_schema.COLUMNS
                WHERE TABLE_NAME = ?
                AND TABLE_SCHEMA = DATABASE()
                """;

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> {
                    ColumnInfo c = new ColumnInfo();
                    c.setName(rs.getString("COLUMN_NAME"));
                    c.setType(rs.getString("COLUMN_TYPE"));
                    c.setNullable("YES".equals(rs.getString("IS_NULLABLE")));
                    return c;
                },
                tableName);
    }

    @Data
    public static class ColumnInfo {
        private String name;
        private String type;
        private boolean nullable;
    }
}