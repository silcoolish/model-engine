package com.ljw.playdough.modelengine.ddl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DdlExecutor {

    private final JdbcTemplate jdbcTemplate;

    public DdlExecutor(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void execute(String sql) {
        log.debug("开始执行DDL: {}", sql);
        jdbcTemplate.execute(sql);
        log.debug("DDL执行成功: {}", sql);
    }

}