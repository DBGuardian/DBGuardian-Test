# DBGuardian 测试示例：Java 8 / Spring Boot 2.7 / Spring JDBC / MariaDB

本项目使用 Spring Boot 2.7.18、Spring JDBC、MariaDB JDBC 驱动以及 DBGuardian MariaDB 方言。写入使用 `JdbcTemplate`，查询使用 `NamedParameterJdbcTemplate`，不依赖 JPA 或 MyBatis。

## 初始化和启动

1. 从本项目目录启动 MariaDB 客户端，连接当前可写主库，执行 `SOURCE src/main/resources/db/schema.sql;`，等待从库同步。脚本沿用 [JPA MariaDB 示例](../dbguardian-test-java8-sb27-jpa-mariadb/README.md) 的 `dbguardian` 库和 `t_user`、`t_order`、`hello` 表。已初始化的环境无需重复导入，应用不会自动建表。
2. 在启动应用的终端或 IDE 运行配置中提供环境变量。验证故障切换时，请使用专用测试环境，并且同一套主从只运行一个管理节点角色的示例应用。所需数据库权限见 [MariaDB 方言使用说明](../../DBGuardian-doc/doc/MariaDB方言使用说明.md)。

| 环境变量 | 用途 |
| --- | --- |
| `DBGUARDIAN_MARIADB_MASTER_URL`、`DBGUARDIAN_MARIADB_SLAVE_URL` | JDBC 地址，默认为 `jdbc:mariadb://127.0.0.1:3306/dbguardian` 和 `jdbc:mariadb://127.0.0.1:3307/dbguardian`（另含连接与读写超时参数） |
| `DBGUARDIAN_MARIADB_USERNAME`、`DBGUARDIAN_MARIADB_PASSWORD` | 数据源账号和密码 |
| `DBGUARDIAN_MARIADB_REPLICATION_USER`、`DBGUARDIAN_MARIADB_REPLICATION_PASSWORD` | 恢复和回切时重新建立复制所用的账号和密码 |
| `DBGUARDIAN_REDIS_HOST`、`DBGUARDIAN_REDIS_PORT`、`DBGUARDIAN_REDIS_PASSWORD`、`DBGUARDIAN_REDIS_DATABASE` | 可选 Redis 状态协调，单实例演示可不启动 Redis |
| `SERVER_PORT` | HTTP 端口，默认为 `8084` |

在仓库根目录使用 Java 8 执行：

```bash
mvn -f DBGuardian-Spring-Boot-Starter/pom.xml -pl dbguardian-boot2-starter,dbguardian-dialect-mariadb -am -DskipTests install
mvn -f DBGuardian-Test/dbguardian-test-java8-sb27-jdbc-mariadb/pom.xml clean verify
java -jar DBGuardian-Test/dbguardian-test-java8-sb27-jdbc-mariadb/target/dbguardian-test-java8-sb27-jdbc-mariadb-1.0.0.jar
```

## 接口和路由验证

| 资源 | 接口（均以 `/api` 开头） |
| --- | --- |
| 用户 | `POST /user`；`GET /user/list`、`/user/{id}`、`/user/by-username/{username}`、`/user/master/{id}`；`PUT`、`DELETE /user/{id}` |
| 订单 | `POST /order`（提供 `orderNo`、`userId`、`amount`，`status` 默认 `PENDING`）；`GET /order/list`、`/order/{id}`、`/order/by-user/{userId}`、`/order/by-order-no/{orderNo}`、`/order/master/{id}`；`PUT`、`DELETE /order/{id}` |
| 路由 | `GET /user/routing`，通过 MariaDB `@@GLOBAL.server_id` 返回普通读取和写事务使用的实际节点编号 |

普通查询优先读从库；写入、写事务内的回查和 `/master/{id}` 始终连接当前写节点。正常情况下 `readServerId` 与 `writeServerId` 分别来自从库和主库；从库升主后两者可以相同，原主回切后恢复分离。可用 `curl http://localhost:8084/api/user/routing` 查看；示例默认启用故障恢复和自动回切，并输出复制健康检查日志。

`mvn test` 使用两套独立的 H2 内存库验证查询和写入路由、订单接口、读流量回落以及模拟角色切换，不需要 MariaDB 或 Redis。实际节点编号、MariaDB 复制和自动回切需在专用双节点环境验证。
