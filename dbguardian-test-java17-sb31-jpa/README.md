# DBGuardian Java 17 / Spring Boot 3.1 / JPA Test

This project verifies DBGuardian read-write splitting with Java 17, Spring Boot 3.1, Spring Data JPA, Hibernate 6, and MySQL 8.

## Run

Configure the `DBGUARDIAN_*` environment variables when the default MySQL and Redis endpoints are not available, then run:

```bash
mvn spring-boot:run
```

The application exposes:

- `GET /api/user/list`
- `GET /api/user/{id}`
- `GET /api/user/by-username/{username}`
- `POST /api/user`
- `DELETE /api/user/{id}`
- `GET /api/datasource/status`

## Tests

```bash
mvn test
```

Tests run against H2 in MySQL compatibility mode and verify the JPA transaction manager plus repository CRUD integration.