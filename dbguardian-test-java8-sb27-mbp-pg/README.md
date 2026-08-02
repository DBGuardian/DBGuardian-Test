# DBGuardian 测试项目 - Java8 / Spring Boot 2.7 / MyBatis-Plus / PostgreSQL

> **技术栈**: `[JAVA8]` `[SPRING_BOOT_27]` `[MYBATIS_PLUS]` `[POSTGRESQL]`

这是一个用于验证 DBGuardian PostgreSQL 读写分离能力的 Spring Boot 2.7 测试项目。

## 技术栈

| 技术 | 版本 | 说明 |
|-----|------|------|
| Java | 8 | JDK |
| Spring Boot | 2.7.18 | Web 框架 |
| MyBatis-Plus | 3.5.3.1 | ORM 框架 |
| PostgreSQL | 15+ | 数据库 |
| Redis | 6.x / 7.x | 分布式协调 |

## 项目结构

```
dbguardian-test-java8-sb27-mbp-pg/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/dbguardian/test/
    │   │   ├── Application.java
    │   │   ├── controller/
    │   │   ├── entity/
    │   │   ├── mapper/
    │   │   └── service/
    │   └── resources/
    │       ├── application.yml
    │       └── db/
    │           └── schema.sql
    └── test/
        ├── java/com/dbguardian/test/
        └── resources/application-test.yml
```

## 数据库准备

示例配置使用两个 PostgreSQL 实例：

| 角色 | 地址 | 数据库 |
|-----|------|--------|
| 主库 | `localhost:5432` | `dbguardian_master` |
| 从库 | `localhost:5433` | `dbguardian_slave` |

先创建数据库：

```sql
CREATE DATABASE dbguardian_master;
CREATE DATABASE dbguardian_slave;
```

然后分别连接到两个数据库，执行：

```bash
psql -h localhost -p 5432 -U postgres -d dbguardian_master -f src/main/resources/db/schema.sql
psql -h localhost -p 5433 -U postgres -d dbguardian_slave -f src/main/resources/db/schema.sql
```

`schema.sql` 包含简单的 `t_user`、`t_order` 表和基础初始化数据。

## 配置说明

默认配置文件：`src/main/resources/application.yml`

关键配置：

```yaml
spring:
  datasource:
    master:
      url: jdbc:postgresql://localhost:5432/dbguardian_master
      username: postgres
      password: postgres
      driver-class-name: org.postgresql.Driver
    slave:
      url: jdbc:postgresql://localhost:5433/dbguardian_slave
      username: postgres
      password: postgres
      driver-class-name: org.postgresql.Driver
    replication:
      master-host: localhost
      master-port: 5432
      master-user: postgres
      master-password: postgres
      auto-reconnect: true
```

DBGuardian Starter 会基于 `spring.datasource.master`、`spring.datasource.slave` 和 `spring.datasource.replication` 构建节点，不需要额外声明 `dbguardian.nodes`。

### PostgreSQL 升主权限

PostgreSQL standby 解除只读需要执行 `pg_promote()`。如果数据源账号不是超级用户，需要在每个可能被提升为主库的 PostgreSQL 实例上授予函数执行权限。

注意：物理 standby 处于 recovery 只读状态，不能直接在从库上执行 `GRANT`。应优先在当前主库执行授权，让权限变更通过 WAL 同步到从库。

示例：当前数据源账号为 `appuser` 时，在当前主库实例执行：

```sql
GRANT EXECUTE ON FUNCTION pg_catalog.pg_promote(boolean, integer) TO appuser;
```

如果当前主库已经不可用，而必须在原从库上处理，需要先使用 PostgreSQL 超级用户手动提升原从库：

```sql
SELECT pg_promote();
GRANT EXECUTE ON FUNCTION pg_catalog.pg_promote(boolean, integer) TO appuser;
```

建议两个节点都在其作为主库时执行同样授权，避免主从切换后角色反转时再次遇到权限不足。

Redis 默认使用 `localhost:6379`，如本地没有 Redis，可按测试目标调整协调配置。

## 启动项目

```bash
mvn spring-boot:run
```

接口入口：

- API 文档：http://localhost:8081/api/doc.html
- 用户列表：http://localhost:8081/api/user/list
- 数据源状态：http://localhost:8081/api/datasource/status

## 测试范围

| 测试类别 | 目标 |
|---------|------|
| 读写分离 | 验证查询和写入使用不同数据源 |
| 注解路由 | 验证强制主库、只读路由等注解行为 |
| 状态观测 | 验证主从状态和数据源状态接口 |
| 故障转移 | 为 PostgreSQL 方言接入后的主从切换验证提供样例工程 |

---

**项目版本**: 1.0.0
**最后更新**: 2026-05-19
