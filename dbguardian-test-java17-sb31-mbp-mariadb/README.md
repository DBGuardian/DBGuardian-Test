# DBGuardian 测试示例：Java 17 / Spring Boot 3.1 / MyBatis-Plus / MariaDB

本项目使用 Java 17、Spring Boot 3.1.12、MyBatis-Plus 3.5.7、DBGuardian Boot 3 Starter 和 MariaDB 方言，演示 MariaDB 10.11+ 单主单从的读写分离。用户和订单通过 `BaseMapper` 和 Lambda 条件构造器实现增删改查，数据库结构与 [Spring Boot 3.0 MariaDB 示例](../dbguardian-test-java17-sb30-mbp-mariadb/README.md) 一致。

## 初始化与启动

1. 从本项目目录启动 MariaDB 客户端，连接当前可写主库，执行 `SOURCE src/main/resources/db/schema.sql;`，等待从库同步。[初始化脚本](src/main/resources/db/schema.sql)创建 `dbguardian` 库和 `t_user`、`t_order`、`hello` 三张表，不删除已有数据；已初始化的环境无需重复导入。应用不会自动建表。
2. 启动前配置下列环境变量。所需权限见 [MariaDB 方言使用说明](../../DBGuardian-doc/doc/MariaDB方言使用说明.md)。本示例默认启用故障恢复和自动回切，只应连接独立测试集群；同一套数据库只运行一个管理节点角色的示例应用。

| 环境变量 | 用途 |
| --- | --- |
| `DBGUARDIAN_MARIADB_MASTER_URL`、`DBGUARDIAN_MARIADB_SLAVE_URL` | 主从 JDBC 地址，默认分别连接本机 `3306` 和 `3307` 端口的 `dbguardian` 库 |
| `DBGUARDIAN_MARIADB_USERNAME`、`DBGUARDIAN_MARIADB_PASSWORD` | 数据源账号和密码 |
| `DBGUARDIAN_MARIADB_REPLICATION_USER`、`DBGUARDIAN_MARIADB_REPLICATION_PASSWORD` | 恢复和回切时重新建立复制所用的账号和密码 |
| `DBGUARDIAN_REDIS_HOST`、`DBGUARDIAN_REDIS_PORT`、`DBGUARDIAN_REDIS_PASSWORD`、`DBGUARDIAN_REDIS_DATABASE` | 可选 Redis 状态协调，单实例演示可不启动 Redis |
| `SERVER_PORT` | HTTP 端口，默认 `8089` |

在仓库根目录使用 JDK 17 执行：

```bash
mvn -f DBGuardian-Spring-Boot-Starter/pom.xml -pl dbguardian-boot3-starter,dbguardian-dialect-mariadb -am -DskipTests install
mvn -f DBGuardian-Test/dbguardian-test-java17-sb31-mbp-mariadb/pom.xml clean verify
java -jar DBGuardian-Test/dbguardian-test-java17-sb31-mbp-mariadb/target/dbguardian-test-java17-sb31-mbp-mariadb-1.0.0.jar
```

## 接口与读写路由

| 资源 | 接口（均以 `/api` 开头） |
| --- | --- |
| 用户 | `POST /user`；`GET /user/list`、`/user/{id}`、`/user/by-username/{username}`、`/user/master/{id}`；`PUT`、`DELETE /user/{id}` |
| 订单 | `POST /order`（提供 `orderNo`、`userId`、`amount`，`status` 默认 `PENDING`）；`GET /order/list`、`/order/{id}`、`/order/by-user/{userId}`、`/order/by-order-no/{orderNo}`、`/order/master/{id}`；`PUT`、`DELETE /order/{id}` |
| 路由 | `GET /user/routing`，通过 MariaDB `@@GLOBAL.server_id` 返回普通查询和写事务实际连接的节点编号 |

普通查询优先读取健康从库；写入、写事务内回查和 `/master/{id}` 查询使用当前写节点。启动后执行 `curl http://localhost:8089/api/user/routing` 可查看 `readServerId` 和 `writeServerId`。从库不可用时，健康检测完成后两个编号应都指向当前写节点；从库恢复且复制健康后，普通查询恢复从读。

`mvn test` 使用两套独立的 H2 内存数据库验证读写路由、事务内回查、从库不可用时回落、模拟升主和回切，以及订单接口。真实节点编号、复制和升主需要在专用 MariaDB 双节点环境中验证。
