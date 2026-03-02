package com.ljw.playdough.modelengine.ddl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class TableValidator {

    private final JdbcTemplate jdbcTemplate;

    public TableValidator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean exists(String tableName) {
        String sql = """
                SELECT COUNT(*)
                FROM information_schema.TABLES
                WHERE TABLE_NAME = ?
                AND TABLE_SCHEMA = DATABASE()
                """;

        Integer count = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                tableName
        );

        return count != null && count > 0;
    }
}