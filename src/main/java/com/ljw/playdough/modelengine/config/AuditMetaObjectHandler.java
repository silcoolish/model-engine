package com.ljw.playdough.modelengine.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AuditMetaObjectHandler implements MetaObjectHandler {

    // TODO: replace with authenticated user context once auth system is integrated
    private static final long SYSTEM_USER_ID = 1L;

    @Override
    public void insertFill(MetaObject metaObject) {

        this.strictInsertFill(metaObject, "createTime",
                LocalDateTime.class, LocalDateTime.now());

        this.strictInsertFill(metaObject, "updateTime",
                LocalDateTime.class, LocalDateTime.now());

        this.strictInsertFill(metaObject, "createBy",
                Long.class, SYSTEM_USER_ID);

        this.strictInsertFill(metaObject, "updateBy",
                Long.class, SYSTEM_USER_ID);
    }

    @Override
    public void updateFill(MetaObject metaObject) {

        this.strictUpdateFill(metaObject, "updateTime",
                LocalDateTime.class, LocalDateTime.now());

        this.strictUpdateFill(metaObject, "updateBy",
                Long.class, SYSTEM_USER_ID);
    }
}