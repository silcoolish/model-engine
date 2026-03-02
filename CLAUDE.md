# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
mvn clean package -DskipTests

# Run
mvn spring-boot:run

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=AppTest

# Package without tests
mvn clean install -DskipTests
```

**Prerequisites:** JDK 17, MySQL 8.0.21 running locally on port 3306 with database `model_engine` created. Default credentials: `root / 123456` (see `application.yml`).

Initialize DB schema before first run:
```sql
-- Run: document/sql/mysql.sql
```

## Architecture

Model Engine is a **metadata-driven data model engine**: it stores model/field definitions as metadata and dynamically manages physical MySQL tables via DDL.

### Layer Overview

```
Controller (REST API)
    ↓
ModelEngineService          ← orchestration facade
    ↓              ↓
SysModelService   SysModelFieldService   ← metadata CRUD (MyBatis-Plus)
    ↓
DDL Layer (ddl/)            ← physical table management
```

### DDL Layer (`com.ljw.playdough.modelengine.ddl`)

The core engine. All classes are stateless utilities or Spring components:

| Class | Role |
|---|---|
| `DdlExecutor` | Executes raw DDL SQL via `JdbcTemplate` |
| `TableSqlGenerator` | Generates `CREATE TABLE` SQL from field definitions |
| `SchemaDiffEngine` | Diffs `newFields` vs live DB columns → `ALTER TABLE` statements |
| `TableMetadataReader` | Reads real column info from MySQL `INFORMATION_SCHEMA` |
| `TableValidator` | Checks if a physical table exists |
| `DdlSecurityUtils` | Validates identifiers (whitelist regex) and maps `FieldType` → SQL type |
| `FieldTypeMapper` | Type mapping reference |

### Model Lifecycle (State Machine)

```
DRAFT → PUBLISHED → OFFLINE → (hard delete)
                 ↓
              DELETED (soft delete, any state)
```

- `updateModelSchema` is only allowed while the physical table exists (typically in DRAFT).
- `hardDeleteModel` requires OFFLINE status; drops the physical table and removes all metadata.
- `softDeleteModel` sets `deleted=1` and status=DELETED without touching the physical table.

### Metadata Tables

- `sys_model` — model definitions (`code` and `table_name` are unique)
- `sys_model_field` — field definitions linked to a model; unique on `(model_id, code)`

Both tables use `BaseEntity` which provides: `id`, `createBy`, `updateBy`, `createTime`, `updateTime`, `deleted`.
Auto-fill is handled by `AuditMetaObjectHandler` (MyBatis-Plus `MetaObjectHandler`).

### API Response Envelope

All endpoints return `Result<T>`:
```json
{ "code": 0, "msg": "success", "data": ... }
{ "code": 400, "msg": "...", "data": null }
```

`BusinessException(code, message)` is caught globally by `GlobalExceptionHandler` and returned as `Result.fail(code, msg)`.

### DDL Security

`DdlSecurityUtils.validateIdentifier` enforces `[a-zA-Z_][a-zA-Z0-9_]*` on all table/column names before any DDL executes. SQL injection through user-supplied identifiers is prevented at this layer.

### FieldType → MySQL Type Mapping

| FieldType | MySQL |
|---|---|
| STRING | VARCHAR(255) (or custom length) |
| TEXT | TEXT |
| INTEGER | INT |
| LONG | BIGINT |
| DECIMAL | DECIMAL(10,2) |
| BOOLEAN | TINYINT(1) |
| DATE | DATE |
| DATETIME | DATETIME |

Note: `DdlSecurityUtils.mapFieldType` currently only handles STRING, INT, DECIMAL, DATE, BOOLEAN — other types from `FieldTypeMapper`/README are not yet wired in.
