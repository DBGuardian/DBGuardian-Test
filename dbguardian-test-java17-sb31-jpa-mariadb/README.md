# DBGuardian 测试示例：Java 17 / Spring Boot 3.1 / JPA / MariaDB

本项目使用 Java 17、Spring Boot 3.1.12、Spring Data JPA（Jakarta Persistence / Hibernate 6）、DBGuardian Boot 3 Starter 和 MariaDB 方言，验证 MariaDB 10.11+ 单主单从环境的读写路由。

## 1. 初始化数据库

在**当前可写主库**执行 [schema.sql](src/main/resources/db/schema.sql)，等待 GTID 复制同步到从库。以下命令从项目目录在 Bash 或 `cmd.exe` 中运行；PowerShell 中可先进入 `mariadb` 客户端，再执行 `SOURCE src/main/resources/db/schema.sql;`。

```bash
mariadb -h <主库地址> -P 3306 -u <初始化账号> -p < src/main/resources/db/schema.sql
```

脚本创建各 MariaDB 示例共用的 `dbguardian` 库和 `t_user`、`t_order`、`hello` 表，不删除已有数据、不创建账号或修改复制配置。`hello` 仅在表为空时插入 `1`，可在主库更新 `num`、等待复制后从从库读取，以验证复制。应用关闭了 Hibernate 自动建表和 Spring SQL 初始化。

## 2. 构建和启动

使用 JDK 17，从仓库根目录执行：

```bash
mvn -f DBGuardian-Spring-Boot-Starter/pom.xml -pl dbguardian-boot3-starter,dbguardian-dialect-mariadb -am -DskipTests install
mvn -f DBGuardian-Test/dbguardian-test-java17-sb31-jpa-mariadb/pom.xml clean verify
```

启动前按实际环境设置变量，或用外部 Spring 配置覆盖：

| 环境变量 | 用途 |
| --- | --- |
| `DBGUARDIAN_MARIADB_MASTER_URL` / `DBGUARDIAN_MARIADB_SLAVE_URL` | 主从节点的 JDBC URL，例如 `jdbc:mariadb://主库:3306/dbguardian` |
| `DBGUARDIAN_MARIADB_USERNAME` / `DBGUARDIAN_MARIADB_PASSWORD` | 数据源账号和密码 |
| `DBGUARDIAN_MARIADB_REPLICATION_USER` / `DBGUARDIAN_MARIADB_REPLICATION_PASSWORD` | 自动重挂复制和回切使用的复制账号和密码 |
| `DBGUARDIAN_REDIS_HOST` / `DBGUARDIAN_REDIS_PORT` / `DBGUARDIAN_REDIS_PASSWORD` / `DBGUARDIAN_REDIS_DATABASE` | 可选 Redis 状态协调；单应用实例可不启动 Redis |
| `SERVER_PORT` | HTTP 端口，默认 `8088` |

示例默认启用故障恢复和自动回切，只应连接独立测试集群。数据源账号还需具备检查复制状态、管理节点角色及复制的权限，详见 [MariaDB 方言使用说明](../../DBGuardian-doc/doc/MariaDB方言使用说明.md)。

```bash
java -jar DBGuardian-Test/dbguardian-test-java17-sb31-jpa-mariadb/target/dbguardian-test-java17-sb31-jpa-mariadb-1.0.0.jar
```

## 3. 验证读写路由

```bash
curl http://localhost:8088/api/user/routing
curl -X POST http://localhost:8088/api/user -H 'Content-Type: application/json' \
  -d '{"username":"jpa-demo","email":"jpa-demo@example.test"}'
curl http://localhost:8088/api/user/list
curl http://localhost:8088/api/user/master/1
```

把最后一条命令中的 `1` 换成创建接口返回的 ID。普通查询优先读健康从库，可能要等异步复制追平；`/master/{id}` 查询当前写节点，适合写入后立刻读取。写操作和同一写事务内的查询都走当前写节点。

| 接口 | 用途 |
| --- | --- |
| `POST /api/user`、`PUT /api/user/{id}`、`DELETE /api/user/{id}` | 创建、更新、删除用户 |
| `GET /api/user/list`、`GET /api/user/{id}`、`GET /api/user/by-username/{username}` | 普通用户查询 |
| `GET /api/user/master/{id}` | 从当前写节点查询用户 |
| `POST /api/order`、`PUT /api/order/{id}`、`DELETE /api/order/{id}` | 创建、更新、删除订单。创建时传入 `orderNo`、`userId`、`amount`，`status` 默认 `PENDING` |
| `GET /api/order/list`、`GET /api/order/{id}`、`GET /api/order/by-user/{userId}`、`GET /api/order/by-order-no/{orderNo}` | 普通订单查询 |
| `GET /api/order/master/{id}` | 从当前写节点查询订单 |

`GET /api/user/routing` 使用 JPA 原生查询读取 MariaDB 的 `@@GLOBAL.server_id`，返回普通读的 `readServerId` 和写事务内读的 `writeServerId`：

| 测试集群状态 | 预期结果 |
| --- | --- |
| 主从均健康 | `readServerId` 指向从库，`writeServerId` 指向主库 |
| 从库不可用 | 健康检测完成后，两个 ID 均指向主库 |
| 原主已隔离，从库完成升主 | 两个 ID 均指向新主库 |
| 原主恢复并完成自动回切，从库重新接入且健康 | 写入回到原主，普通查询恢复从读 |

自动测试使用两套独立的 H2 内存数据库，覆盖从库读取、主库读取、JPA 写事务、订单接口，以及模拟故障回退、升主和回切后的路由。测试不依赖真实 MariaDB 或 Redis，也不验证真实复制或数据库升主；这些场景需要在独立 MariaDB 集群中手动验证。
