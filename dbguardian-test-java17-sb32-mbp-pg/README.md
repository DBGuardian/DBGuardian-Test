# DBGuardian 测试项目 - Java17 / Spring Boot 3.2 / MyBatis-Plus / PostgreSQL

> 技术栈: `[JAVA17]` `[SPRING_BOOT_32]` `[MYBATIS_PLUS]` `[POSTGRESQL]`

用于验证 DBGuardian Boot 3 Starter 在 PostgreSQL 主从场景中的读写路由、健康检查和故障恢复行为。

## 技术栈

| 技术 | 版本 |
|------|------|
| Java | 17 |
| Spring Boot | 3.2.5 |
| MyBatis-Plus | 3.5.7 |
| PostgreSQL | 15+ |
| Redis | 6.x / 7.x |

## 数据库准备

测试配置默认使用 `localhost:5432/dbguardian_master` 作为主库，`localhost:5433/dbguardian_slave` 作为从库。请先准备 PostgreSQL 物理流复制，再分别执行 `src/main/resources/db/schema.sql`。

```bash
createdb -h localhost -p 5432 -U postgres dbguardian_master
createdb -h localhost -p 5433 -U postgres dbguardian_slave
psql -h localhost -p 5432 -U postgres -d dbguardian_master -f src/main/resources/db/schema.sql
psql -h localhost -p 5433 -U postgres -d dbguardian_slave -f src/main/resources/db/schema.sql
```

应用账号需要具备提升 standby 所需的权限：

```sql
GRANT EXECUTE ON FUNCTION pg_catalog.pg_promote(boolean, integer) TO appuser;
```

自动恢复配置使用 `pg_ctl`、`psql` 和 `pg_rewind`。生产环境请按实际 PostgreSQL 安装路径和远程执行方式调整 `application.yml`。

## 执行测试

```bash
mvn install -DskipTests
mvn test
```

测试覆盖基础 Spring 上下文、MyBatis-Plus Mapper 注入、读写路由上下文、协调服务 Bean 以及 PostgreSQL 恢复配置边界。

**项目版本**: 1.0.0
**最后更新**: 2026-08-03