package com.ljw.playdough.modelengine.ddl;



import com.ljw.playdough.modelengine.entity.SysModelField;

import java.util.List;

public class TableSqlGenerator {

    public static String generateCreateTableSql(
            String tableName,
            List<SysModelField> fields) {

        StringBuilder sb = new StringBuilder();
        // 校验表名
        DdlSecurityUtils.validateIdentifier(tableName, "表名");

        sb.append("CREATE TABLE ").append(tableName).append(" (\n");
        sb.append("id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',\n");

        // 业务字段
        for (SysModelField field : fields) {

            // 校验字段名
            DdlSecurityUtils.validateIdentifier(field.getCode(), "字段名");

            // 字段类型映射
            String columnType = DdlSecurityUtils.mapFieldType(
                    field.getFieldType(),
                    field.getLength()
            );

            sb.append(field.getCode()).append(" ").append(columnType);

            if (Boolean.TRUE.equals(field.getRequired())) {
                sb.append(" NOT NULL");
            }
            sb.append(" COMMENT '").append(field.getName().replace("'", "''")).append("',\n");
        }

        // 固定审计字段
        sb.append("""
                create_by BIGINT COMMENT '创建人ID',
                update_by BIGINT COMMENT '更新人ID',
                create_time DATETIME COMMENT '创建时间',
                update_time DATETIME COMMENT '更新时间',
                deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识'
                """);
        sb.append("\n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
        return sb.toString();
    }
}