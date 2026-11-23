# DBGuardian 测试示例：Java 11 / Spring Boot 2.7 / Spring JDBC / MariaDB

本项目使用 Java 11、Spring Boot 2.7.18、MariaDB JDBC 驱动和 DBGuardian MariaDB 方言。写入使用 `JdbcTemplate`，查询使用 `NamedParameterJdbcTemplate`，不依赖 JPA 或 MyBatis。表结构与 [Java 8 Spring JDBC MariaDB 示例](../dbguardian-test-java8-sb27-jdbc-mariadb/README.md) 一致。

## 初始化与启动

1. 从本项目目录启动 MariaDB 客户端，连接**当前可写主库**，执行 `SOURCE src/main/resources/db/schema.sql;` 并等待从库同步。脚本创建 `dbguardian` 库及 `t_user`、`t_order`、`hello` 表；已初始化的环境无需重复导入。应用不会自动建表。
2. 在启动应用的终端或 IDE 运行配置中设置下列环境变量。数据源账号和复制账号所需权限见 [MariaDB 方言使用说明](../../DBGuardian-doc/doc/MariaDB方言使用说明.md)。验证自动切换时，请使用专用主从环境，同一套数据库只运行一个管理节点角色的示例应用。

| 环境变量 | 用途 |
| --- | --- |
| `DBGUARDIAN_MARIADB_MASTER_URL`、`DBGUARDIAN_MARIADB_SLAVE_URL` | 主从 JDBC 地址，默认分别为本机 `3306` 和 `3307` 端口的 `dbguardian` 库 |
| `DBGUARDIAN_MARIADB_USERNAME`、`DBGUARDIAN_MARIADB_PASSWORD` | 数据源账号和密码 |
| `DBGUARDIAN_MARIADB_REPLICATION_USER`、`DBGUARDIAN_MARIADB_REPLICATION_PASSWORD` | 恢复和回切时重新建立复制所用的账号和密码 |
| `DBGUARDIAN_REDIS_HOST`、`DBGUARDIAN_REDIS_PORT`、`DBGUARDIAN_REDIS_PASSWORD`、`DBGUARDIAN_REDIS_DATABASE` | 可选 Redis 状态协调，单应用实例可以不启动 Redis |
| `SERVER_PORT` | HTTP 端口，默认 `8086` |

在仓库根目录使用 JDK 11 执行：

```bash
mvn -f DBGuardian-Spring-Boot-Starter/pom.xml -pl dbguardian-boot2-starter,dbguardian-dialect-mariadb -am -DskipTests install
mvn -f DBGuardian-Test/dbguardian-test-java11-sb27-jdbc-mariadb/pom.xml clean verify
java -jar DBGuardian-Test/dbguardian-test-java11-sb27-jdbc-mariadb/target/dbguardian-test-java11-sb27-jdbc-mariadb-1.0.0.jar
```

## 接口与读写路由

| 资源 | 接口（均以 `/api` 开头） |
| --- | --- |
| 用户 | `POST /user`；`GET /user/list`、`/user/{id}`、`/user/by-username/{username}`、`/user/master/{id}`；`PUT`、`DELETE /user/{id}` |
| 订单 | `POST /order`（提交 `orderNo`、`userId`、`amount`，`status` 默认 `PENDING`）；`GET /order/list`、`/order/{id}`、`/order/by-user/{userId}`、`/order/by-order-no/{orderNo}`、`/order/master/{id}`；`PUT`、`DELETE /order/{id}` |
| 路由 | `GET /user/routing`，通过 MariaDB `@@GLOBAL.server_id` 返回普通读取和写事务实际连接的节点编号 |

普通查询优先走从库，写入、事务内回查及 `/master/{id}` 查询走当前写节点。启动后可通过 `curl http://localhost:8086/api/user/routing` 查看 `readServerId` 和 `writeServerId`。示例默认启用自动恢复和回切，并输出复制健康检查日志。

`mvn test` 使用两套独立的 H2 内存库验证读写分离、读流量回落、模拟切换和回切，以及订单接口，无需启动 MariaDB 或 Redis。真实节点编号、复制和回切需在专用 MariaDB 双节点环境验证。
