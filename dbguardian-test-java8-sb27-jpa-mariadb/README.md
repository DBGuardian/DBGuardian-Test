# DBGuardian 测试示例：Java 8 / Spring Boot 2.7 / JPA / MariaDB

本项目使用 Spring Boot 2.7.18、Spring Data JPA、Hibernate 5 和 `dbguardian-dialect-mariadb`，验证 MariaDB 10.11+ 单主单从环境的读写路由。

## 1. 仅在主库导入初始化 SQL

使用有建库、建表权限的账号，在**当前可写主库**执行 [schema.sql](src/main/resources/db/schema.sql)：

```bash
mariadb -h <主库地址> -P 3306 -u <初始化账号> -p < src/main/resources/db/schema.sql
```

上面的命令从本项目目录在 Bash 或 `cmd.exe` 中运行。在 PowerShell 中可以进入 `mariadb` 客户端后执行 `SOURCE src/main/resources/db/schema.sql;`。

脚本创建供后续 MariaDB 测试项目共用的 `dbguardian` 库，以及 `t_user`、`t_order`、`hello` 三张表。`hello` 只有一个整数列 `num`，仅在表为空时插入 `1`；已改过的数字不会被重复导入覆盖。脚本不会删除已有数据、创建账号或修改复制配置。确保 GTID 复制已配置且允许同步此库，等待结构和初始值复制到从库，**只需在主库导入**。应用关闭了 Hibernate 自动建表和 Spring SQL 初始化。

## 2. 用 `hello` 检查复制和从库只读

1. 在主库执行 `UPDATE dbguardian.hello SET num = 2 WHERE num = 1;`。
2. 在从库执行 `SELECT num FROM dbguardian.hello;`。等待异步复制追平后，预期看到 `2`。
3. 直连从库，用有该表 `UPDATE` 权限、但没有 `READ_ONLY ADMIN` 或 `SUPER` 权限的测试账号执行 `UPDATE dbguardian.hello SET num = 3 WHERE num = 2;`。正常只读从库应拒绝并返回 `ERROR 1290`；再次查询仍应是 `2`。若写入成功，应停止此轮测试并检查连接目标和从库只读配置，避免继续制造位点分叉。

下次测试可把条件中的 `1` 换成当前值，再更新为另一个数字。`hello` 没有应用接口。

如果从库仍能更新，先直连该节点执行 `SELECT @@GLOBAL.server_id, @@GLOBAL.read_only, CURRENT_USER();`，并与 `/api/user/routing` 返回的节点 ID 对照。确认它仍是从库（不是故障切换后升主的节点）后：

- `read_only = 0`：管理员可以在该从库执行 `SET GLOBAL read_only = ON;`，再用普通账号验证。更新并重启 Starter 后，健康检查也会重新开启已确认从库的全局只读；如果管理账号权限不足，应用会暂停从读并记录警告。
- `read_only = 1` 但仍可写：检查 `SHOW GRANTS;` 和启用的角色。用具有表 `UPDATE` 权限、但没有 `READ_ONLY ADMIN` 和 `SUPER` 的账号重试，不要用 `root` 验证从库拒写。

`SET GLOBAL` 不会持久化到 MariaDB 配置。应用未运行时也需要保护从库的环境，应通过数据库部署配置管理启动时的只读状态，并在角色切换后同步更新配置。

## 3. 配置并启动

先从仓库根目录用 Java 8 编译并安装本地 Starter 和方言，再构建本项目：

```bash
mvn -f DBGuardian-Spring-Boot-Starter/pom.xml -pl dbguardian-boot2-starter,dbguardian-dialect-mariadb -am -DskipTests install
mvn -f DBGuardian-Test/dbguardian-test-java8-sb27-jpa-mariadb/pom.xml clean verify
```

启动前按实际环境设置以下变量，或通过外部 `application.yml` 覆盖同名 Spring 配置：

| 环境变量 | 说明 |
| --- | --- |
| `DBGUARDIAN_MARIADB_MASTER_URL` | 主库 JDBC URL，例如 `jdbc:mariadb://主库:3306/dbguardian` |
| `DBGUARDIAN_MARIADB_SLAVE_URL` | 从库 JDBC URL，例如 `jdbc:mariadb://从库:3306/dbguardian` |
| `DBGUARDIAN_MARIADB_USERNAME` / `DBGUARDIAN_MARIADB_PASSWORD` | 两节点的数据源账号和密码 |
| `DBGUARDIAN_MARIADB_REPLICATION_USER` / `DBGUARDIAN_MARIADB_REPLICATION_PASSWORD` | 配置自动重挂复制和回切时使用的复制账号和密码 |
| `DBGUARDIAN_REDIS_HOST` / `DBGUARDIAN_REDIS_PORT` / `DBGUARDIAN_REDIS_PASSWORD` / `DBGUARDIAN_REDIS_DATABASE` | 可选 Redis 状态协调；单应用实例可以不启动 Redis |
| `SERVER_PORT` | HTTP 端口，默认 `8081` |

数据源账号除表的读写权限外，还需具备 Starter 检查复制状态、管理节点角色及复制的权限。数据库配置、权限和隔离旧主的要求见 [MariaDB 方言使用说明](../../DBGuardian-doc/doc/MariaDB方言使用说明.md)。只在测试环境使用本示例；默认启用故障恢复和原主自动回切。

```bash
java -jar DBGuardian-Test/dbguardian-test-java8-sb27-jpa-mariadb/target/dbguardian-test-java8-sb27-jpa-mariadb-1.0.0.jar
```

## 4. 验证应用读写和切换

以下命令使用 Bash 语法，默认地址为 `http://localhost:8081`：

```bash
curl http://localhost:8081/api/user/routing
curl -X POST http://localhost:8081/api/user -H 'Content-Type: application/json' \
  -d '{"username":"maria-demo","email":"maria-demo@example.test"}'
curl http://localhost:8081/api/user/list
curl http://localhost:8081/api/user/master/1
```

最后一条命令请将 `1` 替换为新增接口返回的 ID。`/api/user/{id}` 和 `/api/user/by-username/{username}` 可执行普通查询；`PUT` 和 `DELETE /api/user/{id}` 可更新和删除用户。主库读接口通过 JPA 事务固定到当前写节点，适合写入后立刻查询；普通从库查询可能需要等待异步复制。

订单接口使用相同的路由规则。创建订单前，先通过用户接口取得 `userId`：

```bash
curl -X POST http://localhost:8081/api/order -H 'Content-Type: application/json' \
  -d '{"orderNo":"ORD-001","userId":1,"amount":19.90}'
```

| 方法与路径 | 用途 |
| --- | --- |
| `POST /api/order` | 创建订单，`status` 默认 `PENDING` |
| `GET /api/order/list`、`GET /api/order/{id}` | 查询订单列表或按 ID 查询 |
| `GET /api/order/by-user/{userId}`、`GET /api/order/by-order-no/{orderNo}` | 按用户 ID 或订单号查询 |
| `GET /api/order/master/{id}` | 从当前写节点查询订单 |
| `PUT /api/order/{id}` | 更新订单，发送 `orderNo`、`userId`、`amount`、`status` |
| `DELETE /api/order/{id}` | 删除订单 |

`GET /api/user/routing` 通过 JPA 分别执行普通查询和写事务内的查询，返回数据库实际的 `readServerId` / `writeServerId`（MariaDB `@@GLOBAL.server_id`），而不是预设的角色名称：

| 独立测试环境中的状态 | 预期结果 |
| --- | --- |
| 两节点正常，且从库复制健康 | `readServerId` 为从库，`writeServerId` 为主库 |
| 从库停止，健康检测完成 | 两个 ID 均为主库；从库重接、复制健康后恢复分开 |
| 安全隔离原主，从库完成升主 | 两个 ID 均为升主节点；可重新验证写入 |
| 原主恢复、追平且自动回切完成 | 主库恢复写入，原从库重接并确认健康后恢复从读 |
| 原主恢复，且 `spring.datasource.recovery.auto-failback=false` | 原主只读追赶。追赶完成前两个 ID 都指向升主节点；之后 `readServerId` 为原主，`writeServerId` 仍为升主节点。 |

请在每次模拟故障前确认复制已追平。仅适用单主单从，不测试多从库负载均衡。

回切完成时，Java 后端会以 `INFO` 输出 `GTID 复制状态（恢复完成）`。如需查看回切后每轮健康检查的复制方向、IO/SQL 线程和延迟，参考[查看回切后的复制状态](../../DBGuardian-doc/doc/MariaDB方言使用说明.md#查看回切后的复制状态)开启 `DEBUG`。

项目内的 `mvn test` 使用两套独立的 H2 内存库验证 JPA 读写、写事务、主库查询以及模拟故障状态变化后的路由，不依赖外部 MariaDB 或 Redis。真实复制和升主请在独立 MariaDB 测试环境验证。
