# DBGuardian Spring JDBC 测试项目

技术栈：Java 8、Spring Boot 2.7.18、Spring JDBC、MySQL 8、DBGuardian Boot 2 Starter。

该项目使用 Spring JDBC 复现 `dbguardian-test-java8-sb27-mbp` 的用户接口和数据库脚本，仅将 MyBatis-Plus Mapper 替换为 `JdbcTemplate` 与 `NamedParameterJdbcTemplate`。

## 运行准备

1. 在 MySQL 主库执行 `src/main/resources/db/schema.sql`，并确保数据已复制到从库。
2. 使用 `src/main/resources/application.yml` 中与 MyBatis-Plus 项目一致的主从 MySQL 和 Redis 配置。
3. 先在 `DBGuardian-Spring-Boot-Starter` 安装 `1.0.0` 版本组件，再启动本项目。

```bash
mvn spring-boot:run
```

## 接口

接口路径和响应结构与 `dbguardian-test-java8-sb27-mbp` 保持一致：

- `POST /api/user`：创建用户，使用 `JdbcTemplate.update`，路由到主库。
- `GET /api/user/{id}`：按 ID 查询用户，使用 `NamedParameterJdbcTemplate.query`，路由到从库。
- `GET /api/user/list`：查询用户列表，使用 `NamedParameterJdbcTemplate.query`，路由到从库。
- `GET /api/datasource/status`：获取数据源协调状态。
- `GET /api/datasource/health`：健康检查。

创建用户示例：

```json
{
  "username": "jdbc-user",
  "email": "jdbc-user@test.com",
  "phone": "13800138001",
  "status": 1
}
```