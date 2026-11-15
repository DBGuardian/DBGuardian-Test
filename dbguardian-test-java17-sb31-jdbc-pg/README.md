# DBGuardian Spring JDBC PostgreSQL 测试项目

技术栈：Java 17、Spring Boot 3.1.12、Spring JDBC、PostgreSQL、DBGuardian Boot 3 Starter。

该项目参考 `dbguardian-test-java17-sb31-jdbc` 的 Java 17 / Spring Boot 3.1 / Spring JDBC 用户接口，并参考 `dbguardian-test-java11-sb27-jdbc-pg` 的 PostgreSQL 主从配置、故障恢复配置和数据库脚本。

## 运行准备

1. 在 PostgreSQL 主库执行 `src/main/resources/db/schema.sql`，并确保数据已复制到从库。
2. 根据实际环境调整 `src/main/resources/application.yml` 中的 PostgreSQL 主从、复制、故障恢复和 Redis 配置。
3. 先在 `DBGuardian-Spring-Boot-Starter` 安装 `1.0.0` 版本组件，再启动本项目。

```bash
mvn spring-boot:run
```

## 接口

- `GET /api/user/list`：查询用户列表，路由到从库。
- `GET /api/user/{id}`：按 ID 查询用户，路由到从库。
- `POST /api/user`：创建用户，使用 `JdbcTemplate.update`，路由到主库。
- `PUT /api/user/{id}`：更新用户，使用 `JdbcTemplate.update`，路由到主库。
- `DELETE /api/user/{id}`：删除用户，使用 `JdbcTemplate.update`，路由到主库。
- `GET /api/datasource/status`：获取数据源协调状态。
- `GET /api/datasource/health`：健康检查。

创建用户示例：

```json
{
  "username": "jdbc-java17-pg-user",
  "email": "jdbc-java17-pg-user@test.com",
  "phone": "13800138017",
  "status": 1
}
```
