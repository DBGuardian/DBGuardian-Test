# DBGuardian 测试示例：Java 8 / Spring Boot 2.7 / MyBatis-Plus / MariaDB

本项目使用 Spring Boot 2.7.18、MyBatis-Plus 3.5.3.1、MariaDB JDBC 驱动和 DBGuardian 的 MariaDB 方言。用户和订单的增删改查使用 `BaseMapper` 和 Lambda 条件构造器，与 [JPA MariaDB 示例](../dbguardian-test-java8-sb27-jpa-mariadb/README.md) 共用 `dbguardian` 库和 `t_user`、`t_order`、`hello` 表。

## 初始化与启动

1. 用 MariaDB 客户端连接**当前可写主库**，执行 `SOURCE src/main/resources/db/schema.sql;`（从本项目目录启动客户端），并等待从库同步。已经通过 JPA 或 MyBatis MariaDB 示例初始化的环境无需重复导入。应用不会自动建表。
2. 设置下列环境变量。使用自动切换和回切时，数据源账号需具备查看及管理复制和节点角色的权限，参见 [MariaDB 方言使用说明](../../DBGuardian-doc/doc/MariaDB方言使用说明.md)。用同一套测试数据库验证角色切换时，只启动一个示例应用。

| 环境变量 | 用途 |
| --- | --- |
| `DBGUARDIAN_MARIADB_MASTER_URL`、`DBGUARDIAN_MARIADB_SLAVE_URL` | 主从 JDBC 地址，默认分别使用本机 `3306`、`3307` 端口上的 `dbguardian` 库 |
| `DBGUARDIAN_MARIADB_USERNAME`、`DBGUARDIAN_MARIADB_PASSWORD` | 数据源账号和密码 |
| `DBGUARDIAN_MARIADB_REPLICATION_USER`、`DBGUARDIAN_MARIADB_REPLICATION_PASSWORD` | 恢复和回切时重建复制所用的账号和密码 |
| `DBGUARDIAN_REDIS_HOST`、`DBGUARDIAN_REDIS_PORT`、`DBGUARDIAN_REDIS_PASSWORD`、`DBGUARDIAN_REDIS_DATABASE` | 可选 Redis 状态协调，单实例演示可不启动 Redis |
| `SERVER_PORT` | HTTP 端口，默认 `8083` |

从仓库根目录使用 Java 8 执行：

```bash
mvn -f DBGuardian-Spring-Boot-Starter/pom.xml -pl dbguardian-boot2-starter,dbguardian-dialect-mariadb -am -DskipTests install
mvn -f DBGuardian-Test/dbguardian-test-java8-sb27-mbp-mariadb/pom.xml clean verify
java -jar DBGuardian-Test/dbguardian-test-java8-sb27-mbp-mariadb/target/dbguardian-test-java8-sb27-mbp-mariadb-1.0.0.jar
```

## 接口与读写路由

| 资源 | 接口（均以 `/api` 开头） |
| --- | --- |
| 用户 | `POST /user`；`GET /user/list`、`/user/{id}`、`/user/by-username/{username}`、`/user/master/{id}`；`PUT`、`DELETE /user/{id}` |
| 订单 | `POST /order`（提交 `orderNo`、`userId`、`amount`，`status` 默认 `PENDING`）；`GET /order/list`、`/order/{id}`、`/order/by-user/{userId}`、`/order/by-order-no/{orderNo}`、`/order/master/{id}`；`PUT`、`DELETE /order/{id}` |
| 路由 | `GET /user/routing`：分别读取普通查询和写事务实际连接的 MariaDB `@@GLOBAL.server_id` |

以下为 Bash 示例：

```bash
curl http://localhost:8083/api/user/routing
curl -X POST http://localhost:8083/api/user -H 'Content-Type: application/json' \
  -d '{"username":"mbp-demo","email":"mbp-demo@example.test"}'
```

普通读取优先走从库，`/master/{id}`、写入及同一事务内的回查走当前写节点，避免刚写入的数据因复制延迟而查不到。正常时应看到不同的 `readServerId` 和 `writeServerId`；原主不可用且从库升主后，两者指向升主节点；原主安全回切后恢复分离。示例默认启用恢复与自动回切，并输出复制健康检查日志。可将 `spring.datasource.recovery.auto-failback` 设为 `false`，使原主追平后承担读流量而不接回写流量。

`mvn test` 使用两套独立的 H2 内存库，验证 MyBatis-Plus 查询、写事务、订单接口及模拟角色变化后的路由，不需要运行 MariaDB 或 Redis。真实节点 ID、GTID 复制（全局事务标识）、只读状态和回切需要在专用 MariaDB 双节点测试环境验证。
