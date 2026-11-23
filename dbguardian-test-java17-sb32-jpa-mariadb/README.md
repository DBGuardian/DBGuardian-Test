# DBGuardian 测试示例：Java 17 / Spring Boot 3.2 / JPA / MariaDB

本项目使用 Java 17、Spring Boot 3.2.12、Spring Data JPA（Jakarta Persistence / Hibernate 6.4）、DBGuardian Boot 3 Starter 和 MariaDB 方言，演示 MariaDB 10.11+ 单主单从读写分离。数据库结构与 [Spring Boot 3.1 JPA MariaDB 示例](../dbguardian-test-java17-sb31-jpa-mariadb/README.md) 一致。

## 初始化与启动

1. 从本项目目录启动 MariaDB 客户端，连接**当前可写主库**，执行 `SOURCE src/main/resources/db/schema.sql;`，等待从库同步。[初始化脚本](src/main/resources/db/schema.sql)创建 `dbguardian` 库及 `t_user`、`t_order`、`hello` 表，不删除已有数据。已初始化的环境无需重复导入，应用不会自动建表。
2. 在启动终端或 IDE 运行配置中设置环境变量。所需权限见 [MariaDB 方言使用说明](../../DBGuardian-doc/doc/MariaDB方言使用说明.md)。示例默认启用故障恢复和自动回切，只应连接专用测试集群。

| 环境变量 | 用途 |
| --- | --- |
| `DBGUARDIAN_MARIADB_MASTER_URL`、`DBGUARDIAN_MARIADB_SLAVE_URL` | 主从 JDBC 地址，默认分别连接本机 `3306` 和 `3307` 端口的 `dbguardian` 库 |
| `DBGUARDIAN_MARIADB_USERNAME`、`DBGUARDIAN_MARIADB_PASSWORD` | 数据源账号和密码 |
| `DBGUARDIAN_MARIADB_REPLICATION_USER`、`DBGUARDIAN_MARIADB_REPLICATION_PASSWORD` | 恢复和回切时重新建立复制所用的账号和密码 |
| `DBGUARDIAN_REDIS_HOST`、`DBGUARDIAN_REDIS_PORT`、`DBGUARDIAN_REDIS_PASSWORD`、`DBGUARDIAN_REDIS_DATABASE` | 可选 Redis 状态协调，单应用实例可不启动 Redis |
| `SERVER_PORT` | HTTP 端口，默认 `8091` |

在仓库根目录使用 JDK 17 执行：

```bash
mvn -f DBGuardian-Spring-Boot-Starter/pom.xml -pl dbguardian-boot3-starter,dbguardian-dialect-mariadb -am -DskipTests install
mvn -f DBGuardian-Test/dbguardian-test-java17-sb32-jpa-mariadb/pom.xml clean verify
java -jar DBGuardian-Test/dbguardian-test-java17-sb32-jpa-mariadb/target/dbguardian-test-java17-sb32-jpa-mariadb-1.0.0.jar
```

## 接口与读写路由

| 资源 | 接口（均以 `/api` 开头） |
| --- | --- |
| 用户 | `POST /user`；`GET /user/list`、`/user/{id}`、`/user/by-username/{username}`、`/user/master/{id}`；`PUT`、`DELETE /user/{id}` |
| 订单 | `POST /order`（提交 `orderNo`、`userId`、`amount`，`status` 默认 `PENDING`）；`GET /order/list`、`/order/{id}`、`/order/by-user/{userId}`、`/order/by-order-no/{orderNo}`、`/order/master/{id}`；`PUT`、`DELETE /order/{id}` |
| 路由 | `GET /user/routing`，通过 JPA 原生查询读取 MariaDB 的 `@@GLOBAL.server_id`，分别返回普通查询和写事务实际连接的节点编号 |

普通查询优先读取健康从库；写入、写事务内的查询及 `/master/{id}` 查询使用当前写节点。启动后执行 `curl http://localhost:8091/api/user/routing` 可查看 `readServerId` 和 `writeServerId`。从库不可用时，健康检测完成后两个编号应都指向当前写节点；从库恢复且复制健康后，普通查询恢复从读。

`mvn test` 使用两套独立的 H2 内存库验证主从路由、JPA 写事务、订单接口，以及模拟故障回退、升主和回切后的路由，不需要启动 MariaDB 或 Redis。真实节点编号、复制和升主需要在专用 MariaDB 集群验证。
