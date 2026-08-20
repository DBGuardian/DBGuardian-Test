# DBGuardian Spring JDBC 测试项目

技术栈：Java 21、Spring Boot 3.3.5、Spring JDBC、MySQL 8、DBGuardian Boot 3 Starter。

项目使用 Spring JDBC 复现 `dbguardian-test-java21-sb33-mbp` 的用户、订单接口及数据库脚本。

## 启动

先在 `DBGuardian-Spring-Boot-Starter` 安装 `1.0.0` 组件，并在主库执行 `src/main/resources/db/schema.sql`：

```bash
mvn spring-boot:run
```

## 接口

用户接口：`GET /api/user/list`、`GET /api/user/{id}`、`POST /api/user`、`PUT /api/user/{id}`、`DELETE /api/user/{id}`。

订单接口：`GET /api/order/list`、`GET /api/order/{id}`、`GET /api/order/user/{userId}`、`POST /api/order`、`PUT /api/order/{id}`、`DELETE /api/order/{id}`。

查询使用 `NamedParameterJdbcTemplate` 并路由到从库；新增、更新和删除使用 `JdbcTemplate` 并路由到主库。