# DBGuardian 测试示例：Java 8 / Spring Boot 2.7 / MyBatis / MariaDB

本项目使用 Spring Boot 2.7.18、MyBatis Spring Boot Starter 2.3.1、MariaDB JDBC 驱动及 `dbguardian-dialect-mariadb`，验证 MariaDB 10.11+ 单主单从的读写路由、故障切换和回切。业务 SQL 由 MyBatis 注解 Mapper 执行，不依赖 JPA 或 MyBatis-Plus。

## 1. 初始化数据库

从本项目目录在 Bash 或 `cmd.exe` 中执行，**只在当前可写主库导入**：

```bash
mariadb -h <主库地址> -P 3306 -u <初始化账号> -p < src/main/resources/db/schema.sql
```

在 PowerShell 中可进入 `mariadb` 客户端后执行 `SOURCE src/main/resources/db/schema.sql;`。此脚本与 [JPA MariaDB 示例](../dbguardian-test-java8-sb27-jpa-mariadb/README.md) 共用 `dbguardian` 库及 `t_user`、`t_order`、`hello` 三张表。已经初始化的环境不用重复导入；等待结构和数据复制到从库。应用启动不会自动建表。`hello` 没有 HTTP 接口，可在主库更新 `num`，再到从库查询，以检查复制是否正常。

## 2. 构建和启动

在仓库根目录用 Java 8 执行：

```bash
mvn -f DBGuardian-Spring-Boot-Starter/pom.xml -pl dbguardian-boot2-starter,dbguardian-dialect-mariadb -am -DskipTests install
mvn -f DBGuardian-Test/dbguardian-test-java8-sb27-mybatis-mariadb/pom.xml clean verify
```

通过环境变量或外部 Spring Boot 配置设置连接，不要把真实地址和密码写入仓库：

| 环境变量 | 用途 |
| --- | --- |
| `DBGUARDIAN_MARIADB_MASTER_URL`、`DBGUARDIAN_MARIADB_SLAVE_URL` | 两个节点的 JDBC 地址，例如 `jdbc:mariadb://localhost:3306/dbguardian` 和 `jdbc:mariadb://localhost:3307/dbguardian` |
| `DBGUARDIAN_MARIADB_USERNAME`、`DBGUARDIAN_MARIADB_PASSWORD` | 两节点的数据源账号和密码；可用外部配置分别覆盖 |
| `DBGUARDIAN_MARIADB_REPLICATION_USER`、`DBGUARDIAN_MARIADB_REPLICATION_PASSWORD` | 自动重挂复制使用的账号和密码 |
| `DBGUARDIAN_REDIS_HOST`、`DBGUARDIAN_REDIS_PORT`、`DBGUARDIAN_REDIS_PASSWORD`、`DBGUARDIAN_REDIS_DATABASE` | 可选 Redis 状态协调；单应用实例可以不启动 Redis |
| `SERVER_PORT` | HTTP 端口，默认 `8082` |

数据源账号需要业务表读写权限，以及检查复制状态、管理复制和切换节点角色所需的权限。参见 [MariaDB 方言使用说明](../../DBGuardian-doc/doc/MariaDB方言使用说明.md)。示例默认启用故障恢复及自动回切，仅在测试环境使用；验证同一套数据库的切换时，请只运行一个示例应用，避免两个应用同时管理节点角色。

```bash
java -jar DBGuardian-Test/dbguardian-test-java8-sb27-mybatis-mariadb/target/dbguardian-test-java8-sb27-mybatis-mariadb-1.0.0.jar
```

## 3. 验证接口和路由

以下命令使用 Bash 语法。普通查询优先走从库；`/master/{id}` 和写事务走当前写节点。写入后的同一事务内会从当前写节点回查生成的 ID 对应的记录，不受复制延迟影响。

```bash
curl http://localhost:8082/api/user/routing
curl -X POST http://localhost:8082/api/user -H 'Content-Type: application/json' \
  -d '{"username":"mybatis-demo","email":"mybatis-demo@example.test"}'
```

| 资源 | 接口（均以 `/api` 开头） |
| --- | --- |
| 用户 | `POST /user`；`GET /user/list`、`/user/{id}`、`/user/by-username/{username}`、`/user/master/{id}`；`PUT`、`DELETE /user/{id}` |
| 订单 | `POST /order`（提交 `orderNo`、`userId`、`amount`，`status` 默认 `PENDING`）；`GET /order/list`、`/order/{id}`、`/order/by-user/{userId}`、`/order/by-order-no/{orderNo}`、`/order/master/{id}`；`PUT`、`DELETE /order/{id}` |

`GET /api/user/routing` 在普通查询和写事务中分别查询 `@@GLOBAL.server_id`，返回实际的 `readServerId`、`writeServerId`。正常时两者分别是从库和主库。故障切换后两者都指向升主节点；原主安全回切后恢复分离。若设为 `spring.datasource.recovery.auto-failback=false`，原主追平后承担读流量，升主节点继续写入。示例默认输出 `DEBUG` 级别的复制健康检查日志，可查看当前复制方向、线程状态及延迟。

`mvn test` 使用两套独立的 H2 内存数据库验证 MyBatis 查询、写事务、订单接口及模拟角色变化后的路由，不需要 MariaDB 或 Redis。真实 GTID 复制、只读和回切须在独立的 MariaDB 双节点环境验证。
