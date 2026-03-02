package com.ljw.playdough.modelengine.ddl;


import com.ljw.playdough.modelengine.common.BusinessException;

public class DdlSecurityUtils {

    /**
     * 检查表名或列名是否合法
     */
    public static void validateIdentifier(String name, String type) {
        if (name == null || !name.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            throw new BusinessException(400, "非法" + type + ": " + name);
        }
    }

    /**
     * 根据字段类型映射数据库类型，限制长度
     */
    public static String mapFieldType(String fieldType, Integer length) {
        switch (fieldType.toUpperCase()) {
            case "STRING":
                if (length == null) length = 255;
                if (length <= 0 || length > 65535) {
                    throw new BusinessException(400, "非法字段长度: " + length);
                }
                return "VARCHAR(" + length + ")";
            case "TEXT":
                return "TEXT";
            case "INTEGER":
                return "INT";
            case "LONG":
                return "BIGINT";
            case "DECIMAL":
                return "DECIMAL(10,2)";
            case "BOOLEAN":
                return "TINYINT(1)";
            case "DATE":
                return "DATE";
            case "DATETIME":
                return "DATETIME";
            default:
                throw new BusinessException(400, "不支持的字段类型: " + fieldType);
        }
    }
}