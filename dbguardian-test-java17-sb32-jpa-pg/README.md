# DBGuardian Test: Java 17 / Spring Boot 3.2 / JPA / PostgreSQL

该项目验证 `dbguardian-boot3-starter` 在 Spring Data JPA、Hibernate 6 与 PostgreSQL 主从环境中的读写路由行为。

## 覆盖范围

- `JpaRepository` 的查询和写入分类。
- `JpaTransactionManager` 作为默认事务管理器。
- PostgreSQL 主从恢复、追赶和故障回切配置。
- 测试 profile 使用 H2 PostgreSQL 兼容模式，不依赖外部 PostgreSQL 或 Redis。

## 运行

```bash
mvn test
```

真实 PostgreSQL 主从库初始化使用 `src/main/resources/db/schema.sql`。