-- ========================================
-- 系统模型表：sys_model
-- 用于存储数据模型的元信息
-- ========================================
CREATE TABLE IF NOT EXISTS sys_model (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '模型名称',
    code VARCHAR(100) NOT NULL UNIQUE COMMENT '模型唯一标识',
    table_name VARCHAR(100) NOT NULL UNIQUE COMMENT '对应物理表名',
    description TEXT COMMENT '模型描述',
    status VARCHAR(20) DEFAULT 'DRAFT' COMMENT '模型状态',
    version INT DEFAULT 1 COMMENT '模型版本号',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_by BIGINT COMMENT '创建人ID',
    update_by BIGINT COMMENT '更新人ID',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间'
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='元数据模型表';

-- ========================================
-- 系统模型字段表：sys_model_field
-- 用于存储数据模型字段的元信息
-- ========================================
CREATE TABLE IF NOT EXISTS sys_model_field (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    model_id BIGINT NOT NULL COMMENT '关联模型ID',
    name VARCHAR(100) NOT NULL COMMENT '字段名称',
    code VARCHAR(100) NOT NULL COMMENT '字段标识',
    field_type VARCHAR(50) NOT NULL COMMENT '字段类型 (STRING, TEXT, INTEGER, LONG, DECIMAL, BOOLEAN, DATE, DATETIME)',
    length INT DEFAULT NULL COMMENT '字段长度，可选',
    required BOOLEAN DEFAULT FALSE COMMENT '是否必填',
    default_value VARCHAR(255) DEFAULT NULL COMMENT '默认值',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_by BIGINT COMMENT '创建人ID',
    update_by BIGINT COMMENT '更新人ID',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',

    UNIQUE KEY uq_model_field(model_id, code)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='元数据模型字段表';
