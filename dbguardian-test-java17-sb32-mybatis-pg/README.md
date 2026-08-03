# DBGuardian Test - Java 17 / Spring Boot 3.2 / MyBatis / PostgreSQL

This project verifies the Boot 3 starter with plain MyBatis and PostgreSQL physical replication.

## Stack

| Component | Version |
| --- | --- |
| Java | 17 |
| Spring Boot | 3.2.5 |
| MyBatis | 3.0.4 |
| PostgreSQL | 15+ |

## Database Setup

The default runtime configuration uses `192.168.3.150:5432/appdb` as primary and `192.168.3.151:5432/appdb` as standby. Configure PostgreSQL physical streaming replication before running the application.

Apply `src/main/resources/db/schema.sql` to the primary. The schema will replicate to the standby.

```bash
psql -h 192.168.3.150 -p 5432 -U appuser -d appdb -f src/main/resources/db/schema.sql
```

The PostgreSQL recovery section in `application.yml` contains the `pg_ctl`, `psql`, and `pg_rewind` settings for original-primary catch-up. Update paths, hosts, and remote execution commands for the target environment.

## Run

```bash
mvn install -DskipTests
mvn test
mvn spring-boot:run
```

The user APIs are available below the `/api` context path:

- `POST /api/user`
- `GET /api/user/{id}`
- `GET /api/user/list`