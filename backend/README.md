# CineSchedule Backend

a Backend written in Java using Vert.x and Maven. Includes user authentication (JWT), password hashing, and a simple API.

📋 **[View Full Design Document](../design/DesignDocument.md)** - Complete backend architecture, API specifications, and design patterns.

## Requirements

- JDK 17+
- Maven 3.6+
- (Optional) IntelliJ IDEA for running and debugging

## IntelliJ Run Configurations

Two ready-to-use Maven run configurations live under `.idea/runConfigurations/`:

- `Maven - Test All` runs `mvn test` (tests use in-memory H2; runtime uses MySQL).
- `Maven - Run Backend` runs the Vert.x app via `mvn vertx:run` using the same environment defaults.

Feel free to duplicate them for other environments (e.g., Postgres) by changing the `JDBC_URL`, `DB_USER`, `DB_PASS`, and `JWT_SECRET` entries inside each XML file.

## Build

From the project root:

```bash
mvn clean package
```

This produces an executable jar in `target/`.

If you don't have Maven available on your system, run the project from your IDE (IntelliJ/Idea) which will download dependencies automatically.

## Run

Run the packaged jar:

```bash
java -jar target/CineSchedule-1.0-SNAPSHOT.jar
```

Or run from your IDE (run `org.cineschedule.MainApp`).

Notes about JDBC drivers:

- Runtime database is MySQL. Tests and local prototyping can use in-memory H2; ensure the correct MySQL JDBC driver is available (included in `pom.xml`).

## Tests

Run unit tests:

```bash
mvn test
```

## Test Users

For testing the API, you can use these pre-configured accounts (defined in `V1__create_users.sql`):

| Name    | Email                      | Password     | User ID | Description                          |
| ------- | -------------------------- | ------------ | ------- | ------------------------------------ |
| Alice   | `alice@cineschedule.com`   | `alice123`   | 1       | Test user with sample watchlist data |
| Bob     | `bob@cineschedule.com`     | `bob123`     | 2       | Test user with watched movies        |
| Charlie | `charlie@cineschedule.com` | `charlie123` | 3       | Test user with timeline data         |

### Example API Usage

```bash
# Login to get JWT token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@cineschedule.com","password":"alice123"}'

# Use the token in subsequent requests
curl -X GET http://localhost:8080/api/watchlist \
  -H "Authorization: Bearer <your-jwt-token>"
```

**Note:** These are test credentials only. Passwords are hashed using BCrypt in the migration file. In production, always use strong, unique passwords and proper secrets management.
