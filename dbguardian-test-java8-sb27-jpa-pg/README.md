# DBGuardian Test: Java 8 / Spring Boot 2.7 / JPA / PostgreSQL

该项目验证 `dbguardian-boot2-starter` 在 Spring Data JPA / Hibernate 5 与 PostgreSQL 主从环境中的路由行为。

## 覆盖范围

- `JpaRepository` 查询路由至从库，写入路由至主库。
- `@Transactional` 写事务使用 `JpaTransactionManager`，并固定主库连接。
- `@ForceMaster` 的服务入口用于强制主库查询。
- PostgreSQL 主从恢复参数与 `dbguardian-dialect-postgresql` 对齐。
- `test` profile 使用 H2 PostgreSQL 兼容模式，不依赖外部 PostgreSQL 或 Redis。

## 运行

```bash
mvn test
```

默认运行配置中的数据库密码通过以下环境变量传入：

- `DBGUARDIAN_PG_PASSWORD`
- `DBGUARDIAN_PG_REPLICATION_PASSWORD`
- `DBGUARDIAN_REDIS_PASSWORD`

初始化真实 PostgreSQL 主从库前，请在两个节点的目标数据库执行 `src/main/resources/db/schema.sql`。