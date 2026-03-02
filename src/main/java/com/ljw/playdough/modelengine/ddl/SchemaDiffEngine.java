package com.ljw.playdough.modelengine.ddl;


import com.ljw.playdough.modelengine.entity.SysModelField;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SchemaDiffEngine {

    public static List<String> generateAlterSql(
            String tableName,
            List<SysModelField> newFields,
            List<TableMetadataReader.ColumnInfo> dbColumns) {

        // 校验表名
        DdlSecurityUtils.validateIdentifier(tableName, "表名");

        Map<String, TableMetadataReader.ColumnInfo> dbMap = dbColumns.stream()
            .collect(Collectors.toMap(
                    c -> c.getName().toLowerCase(),
                    c -> c
            ));

        List<String> alterSqlList = new ArrayList<>();

        for (SysModelField field : newFields) {

            String columnName = field.getCode().toLowerCase();

            // 系统字段跳过
            if (isSystemColumn(columnName)) {
                continue;
            }

            if (!dbMap.containsKey(columnName)) {
                // 新增字段
                //
                DdlSecurityUtils.validateIdentifier(field.getCode(), "字段标识");
                String columnType = DdlSecurityUtils.mapFieldType(field.getFieldType(), field.getLength());
                String sql = "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnType;
                alterSqlList.add(sql);

            } else {

                // 字段存在 → 检查是否需要修改
                TableMetadataReader.ColumnInfo dbCol = dbMap.get(columnName);
                String newType = DdlSecurityUtils.mapFieldType(field.getFieldType(), field.getLength());
                if (!dbCol.getType().equalsIgnoreCase(newType)) {
                    String sql = "ALTER TABLE " + tableName + " MODIFY COLUMN " + columnName + " " + newType;
                    alterSqlList.add(sql);
                }
            }
        }

        return alterSqlList;
    }

    private static boolean isSystemColumn(String columnName) {
        return List.of(
                "id",
                "create_by",
                "update_by",
                "create_time",
                "update_time",
                "deleted"
        ).contains(columnName);
    }
}