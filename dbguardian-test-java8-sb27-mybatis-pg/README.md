# DBGuardian 测试项目 - Java8 / Spring Boot 2.7 / MyBatis / PostgreSQL

> 技术栈: `[JAVA8]` `[SPRING_BOOT_27]` `[MYBATIS]` `[POSTGRESQL]`

用于验证 `dbguardian-boot2-starter` 在 PostgreSQL 主从场景中的读写路由、健康检查和从库恢复行为。

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 8 | JDK |
| Spring Boot | 2.7.18 | 应用框架 |
| MyBatis | 2.3.1 | 数据访问框架 |
| PostgreSQL | 15+ | 主从数据库 |
| Redis | 6.x / 7.x | 分布式协调 |

## 项目结构

```
dbguardian-test-java8-sb27-mybatis-pg/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/dbguardian/test/
    │   └── resources/
    │       ├── application.yml
    │       └── db/schema.sql
    └── test/
        ├── java/com/dbguardian/test/
        └── resources/application-test.yml
```

## 数据库准备

测试环境默认使用两个 PostgreSQL 实例。

| 角色 | 地址 | 数据库 |
|------|------|--------|
| 主库 | `localhost:5432` | `dbguardian_master` |
| 从库 | `localhost:5433` | `dbguardian_slave` |

创建数据库并初始化表结构。

```bash
psql -h localhost -p 5432 -U postgres -c "CREATE DATABASE dbguardian_master"
psql -h localhost -p 5433 -U postgres -c "CREATE DATABASE dbguardian_slave"
psql -h localhost -p 5432 -U postgres -d dbguardian_master -f src/main/resources/db/schema.sql
psql -h localhost -p 5433 -U postgres -d dbguardian_slave -f src/main/resources/db/schema.sql
```

实际主从测试需要为两个实例配置物理流复制。`application-test.yml` 仅定义测试连接与恢复参数，不会自动创建 PostgreSQL 集群。

## PostgreSQL 故障转移权限

DBGuardian 在提升 standby 时需要调用 `pg_promote()`。应在当前主库授予应用账号执行权限，让权限经 WAL 同步到从库。

```sql
GRANT EXECUTE ON FUNCTION pg_catalog.pg_promote(boolean, integer) TO appuser;
```

自动恢复通过 `pg_rewind` 重建从库。生产配置中的 `pg_ctl`、`psql`、`pg_rewind`、数据目录和远程执行命令必须与 PostgreSQL 实际安装环境一致。

## 执行

先在仓库根目录安装 DBGuardian 本地依赖，再在本项目目录执行测试。

```bash
mvn install -DskipTests
cd DBGuardian-Test/dbguardian-test-java8-sb27-mybatis-pg
mvn test
```

## 覆盖范围

- PostgreSQL 主库写入、从库读取路由
- 事务与强制主库路由
- PostgreSQL 从库健康检查
- 从库恢复后执行复制重建，且仅在流复制确认后恢复读路由

---

**项目版本**: 1.0.0
**最后更新**: 2026-08-03