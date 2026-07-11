# Data Source Service — Implementation Roadmap

A dependency-ordered build plan: each step only relies on things already built
in a prior step, so you should be able to demo what you just wrote in
isolation before moving on. If you find yourself needing to stub out
something from a *later* step to test the current one, that's a signal
you've jumped ahead — back up.
 
---

### 1. Project skeleton & config

Set up the Spring Boot project with the package structure from the design
doc (`api`, `domain`, `repository`, `service`, `connector`, `security`,
`config`). Wire up MongoDB connection config, `application.yml`, and the env
var for `INSIGHTHUB_ENCRYPTION_KEY` (even if unused yet). No business logic
here — just a service that boots and connects to Mongo.

### 2. Domain entities & tenant-scoped repositories

Create the `DataSource` entity matching the `data_sources` schema, plus
`ConnectorType` enum and `ConnectionConfig` POJO. Build the Mongo repository
with tenant-scoped methods (`findByIdAndTenantId`, `findByTenantId` — never
a bare `findById`). Wire up basic CRUD controllers/endpoints (register,
list, get, update, delete) that only persist metadata — no real DB
connection yet.

### 3. Connector abstraction (interface + factory)

Define the `DataConnector` interface and `ConnectorFactory` exactly as in
the doc. Even with only MySQL coming next, get this scaffolding in place now
so the factory pattern is proven before you write the MySQL-specific logic.

### 4. `MySqlConnector` + test-connection endpoint

Implement `testConnection()` with a real, throwaway JDBC connection (no
pooling yet) to validate credentials. Wire up `POST /test-connection` and
the credential-testing part of `POST /data-sources`. This proves "can we
actually reach a customer's MySQL" in isolation, before pooling adds
complexity.

### 5. Credential encryption (Jasypt)

Add Jasypt/AES encryption now, right after the connector works with
plaintext. Encrypt only at the point of saving to Mongo; decrypt only
transiently when building a connection. Introduce the
`DecryptedConnectionConfig` (or equivalent) so decrypted passwords can't
accidentally leak into logs, persistence, or API responses.

### 6. `ConnectionPoolManager` (HikariCP)

Build the pool manager keyed by `{tenantId}:{dataSourceId}`, using the Week
1 Hikari settings from the doc. Wire pool teardown into the delete (and
update) endpoints — this is easy to forget and is an explicit acceptance
criterion.

### 7. Schema introspection & table selection

Implement `introspectSchema()` using the now-working pool to query
`information_schema`. Wire up `GET /schema` and `PUT /selected-tables`.
This is a good end-to-end smoke test (Mongo → decrypt → pool → connector →
real MySQL query → response) before tackling the more complex
read-with-pagination logic.

### 8. On-demand read, stats recording, tests & docs

Implement `readTable()` with streaming pagination and table-name allow-list
validation against `selectedTables`. Wrap every pool acquisition and query
with `connection_stats` recording (success and failure paths, including
pool health via `getHikariPoolMXBean()`). Finish with service-layer unit
tests, the Postman collection, and the README — write the README last, once
you know the real setup steps.
 
---

## Suggested Pacing

| Steps | Day |
|---|---|
| 1–2 | Day 1 |
| 3–5 | Day 2 — **code review checkpoint after this day** |
| 6 | Day 3 |
| 7 | Day 3 / 4 |
| 8 | Day 4–5 |
 