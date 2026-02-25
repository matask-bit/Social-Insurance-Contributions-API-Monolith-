## Social Insurance Contributions API

### How to run

1. **Start PostgreSQL (DB-only for local dev)**

   ```bash
   docker compose up -d
   ```

   Postgres runs on host port **5433** (container 5432). Database: `insurance_db`, user/password: `postgres`/`postgres`.

2. **Run the Spring Boot application**

   - **On host** (default, uses `dev` profile → `localhost:5433`):

     ```bash
     mvn spring-boot:run
     ```

   - **Inside Docker network** (only if the app runs in Docker and talks to the `postgres` service):

     ```bash
     mvn spring-boot:run -Dspring-boot.run.profiles=docker
     ```

3. **Run tests**

   - Unit tests: `mvn test`
   - Integration tests (requires `docker compose up -d`, uses profile `test`):

     ```bash
     mvn verify -Pintegration
     ```

4. **API documentation**

   - Swagger UI: `http://localhost:8080/swagger-ui/index.html`

