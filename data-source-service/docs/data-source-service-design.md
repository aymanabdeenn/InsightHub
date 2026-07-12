# Design Document: Data Source Service

|            |                     |
| ---------- | ------------------- |
| **Author** | [aymanabdeenn]      |
| **Team**   | InsightHub Platform |
| **Status** | Draft               |

---

## 1. Overview

The Data Source Service is the on-ramp that lets InsightHub read data from a
tenant's external database (MySQL, in Phase 1) on demand, without InsightHub
ever storing the customer's actual data. It is the trusted intermediary
between a tenant's infrastructure and everything InsightHub builds on top of
it — analytics, warehousing, and reporting are all downstream consumers of
this service, not part of it.

In one sentence: **this service is the secure, per-tenant pipe that lets
InsightHub read a customer's database on demand — it does not analyze,
transform, or store that data.**

---

## 2. Context / Problem Statement

InsightHub sells AI analytics to small business owners. To run analytics, we
need their business data — but that data lives in databases the customer
owns and operates, not on InsightHub's infrastructure.

Without a dedicated service for this, every analytics feature would need to
reinvent "how do I safely connect to a customer's database," and customers
would need to either export data manually or grant ad-hoc DB access to
engineers — neither of which scales or is secure.

This service exists to be the **single, reusable chokepoint** through which
all external data access flows, so that:

- Customers onboard once (register credentials, pick tables) and never touch
  integration code.
- Every future consumer (analytics, warehousing, reporting) reuses the same
  connection, isolation, and security guarantees instead of rebuilding them.

**Phase 1 scope**: on-demand reads only, nothing persisted beyond metadata
and usage statistics. Later phases add warehousing, async sync, and caching
on top of this foundation.

---

## 3. Goals / Non-Goals

### Goals (In Scope)

- Register, view, update, delete MySQL data sources.
- Test a connection before saving it.
- Introspect schema (tables + columns) via `information_schema`.
- Let the tenant select which tables are queryable.
- Query data on demand from selected tables, paginated, returned directly in
  the response — no storage.
- Encrypt connection credentials at rest.
- Isolate connection pools per tenant _and_ per data source.
- Record connection/query statistics in MongoDB.

### Non-Goals (Out of Scope for This Phase)

- PostgreSQL, MongoDB, or REST connectors (planned Week 2+).
- Storing synced/replicated customer data.
- Scheduled or async sync jobs.
- A caching layer.
- Payment/entitlement checks (Week 4).
- Authentication — this service trusts `X-Tenant-Id` as set by the Gateway.
- Frontend UI.

---

## 4. Glossary

| Term                  | Meaning                                                                 |
| --------------------- | ----------------------------------------------------------------------- |
| Tenant                | Customer organization; every request belongs to one tenant (`tenantId`) |
| Data Source           | A registered connection to an external database (`dataSourceId`)        |
| Connector             | Code component that knows how to talk to a specific database type       |
| Connection Pool       | HikariCP-managed cache of open DB connections, reused across requests   |
| Connection Statistics | Metadata about pool usage and query activity, stored in MongoDB         |

---

## 5. Proposed Design

### 5.1 High-Level Flow

```
Tenant's MySQL DB  ⇄  Data Source Service (this doc)  →  [future] Analytics Service → Tenant
```

This service is the **first hop only**. It connects, reads, and reports
statistics — it does not interpret or aggregate data.

### 5.2 Request Flow (On-Demand Read — the core path)

1. Tenant registers a data source → we validate input, test the connection
   live, encrypt the password, persist metadata to `data_sources`.
2. Tenant introspects schema → we query `information_schema` through a
   pooled connection and return tables/columns.
3. Tenant selects which tables are queryable → stored on the `data_sources`
   document.
4. Tenant requests data from a selected table (paginated) → we acquire a
   connection from the tenant+data-source-scoped pool, stream the result set,
   return rows directly in the response. Nothing is persisted.
5. Every connection acquisition and query — success or failure — is recorded
   as a `connection_stats` document, including a snapshot of pool health.
6. Deleting or updating a data source tears down its connection pool.

### 5.3 Package Structure

```
com.insighthub.datasource
├── api                  # REST controllers
├── domain               # Entities, enums
├── repository           # Spring Data Mongo
├── service              # Business logic
├── connector
│   ├── DataConnector             # Interface
│   ├── ConnectorFactory          # Factory
│   ├── sql/MySqlConnector        # MySQL impl
│   └── pool/ConnectionPoolManager
├── security             # Credential encryption
└── config
```

### 5.4 Connector Abstraction (Factory Pattern)

```java
public interface DataConnector {
    ConnectorType getType();
    void testConnection(ConnectionConfig config);
    List<TableMetadata> introspectSchema(String dataSourceId);
    Page<Map<String, Object>> readTable(String dataSourceId, String tableName, int page, int size);
}

@Component
public class ConnectorFactory {
    private final Map<ConnectorType, DataConnector> connectors;

    public ConnectorFactory(List<DataConnector> list) {
        this.connectors = list.stream()
            .collect(Collectors.toMap(DataConnector::getType, c -> c));
    }

    public DataConnector get(ConnectorType type) { /* ... */ }
}
```

Spring autowires every `DataConnector` bean into the factory's map. Adding a
new connector type (e.g., `PostgresConnector`) later requires **zero changes**
to the factory — only a new enum value and a new class.

`ConnectorType` is a plain enum (`MYSQL`, later `POSTGRES`, ...) — it's the
factory's map key and is also the value stored on `DataSource.type`.

`ConnectionConfig` is a generic POJO (`host`, `port`, `database`, `username`,
`passwordEncrypted`) shared across connector types — it holds _how to reach_
a database, not _what kind_ of database it is. A separate, transient
`DecryptedConnectionConfig` (plaintext password, never persisted, never
logged, never serialized in a response) is used only at the moment of
building a Hikari pool.

### 5.5 Connection Pool Management

`ConnectionPoolManager` caches one `HikariDataSource` per data source, keyed
by **`{tenantId}:{dataSourceId}`** — never by `dataSourceId` alone (see
§6 Security Considerations for why).

Week 1 Hikari settings:

| Setting             | Value                            |
| ------------------- | -------------------------------- |
| `maximumPoolSize`   | 5                                |
| `minimumIdle`       | 1                                |
| `idleTimeout`       | 5 minutes                        |
| `connectionTimeout` | 10 seconds                       |
| `poolName`          | `pool-{tenantId}-{dataSourceId}` |

Pools are created lazily on first use, reused after that, and explicitly
closed and removed when a data source is deleted or its connection details
are updated.

### 5.6 Credential Encryption

- Jasypt (`jasypt-spring-boot-starter`) with AES.
- Master key supplied via `INSIGHTHUB_ENCRYPTION_KEY` env var — never
  hardcoded, never committed.
- Encrypted before saving to Mongo; decrypted only transiently, in memory,
  when constructing a `HikariDataSource`.
- Decrypted values never appear in logs or API responses.

### 5.7 Data Model

**`data_sources`**

```json
{
  "_id": "uuid",
  "tenantId": "uuid",
  "name": "My Sales DB",
  "type": "MYSQL",
  "connectionConfig": {
    "host": "db.customer.com",
    "port": 3306,
    "database": "sales",
    "username": "readonly_user",
    "passwordEncrypted": "ENC(...)"
  },
  "selectedTables": ["orders", "customers"],
  "status": "ACTIVE",
  "createdAt": "...",
  "updatedAt": "..."
}
```

**`connection_stats`** (recorded on every pool acquisition + query)

```json
{
  "_id": "uuid",
  "tenantId": "uuid",
  "dataSourceId": "uuid",
  "eventType": "QUERY",
  "tableName": "orders",
  "durationMs": 142,
  "rowCount": 100,
  "status": "SUCCESS",
  "errorMessage": null,
  "poolStats": {
    "activeConnections": 2,
    "idleConnections": 3,
    "totalConnections": 5,
    "waitingThreads": 0
  },
  "timestamp": "..."
}
```

Pool stats are read from `HikariDataSource.getHikariPoolMXBean()`.

### 5.8 API Contract

All endpoints require an `X-Tenant-Id` header (set upstream by the Gateway).

| Method | Path                                                                | Purpose              |
| ------ | ------------------------------------------------------------------- | -------------------- |
| POST   | `/api/v1/data-sources/test-connection`                              | Test without saving  |
| POST   | `/api/v1/data-sources`                                              | Register             |
| GET    | `/api/v1/data-sources`                                              | List                 |
| GET    | `/api/v1/data-sources/{id}`                                         | Get one              |
| PUT    | `/api/v1/data-sources/{id}`                                         | Update               |
| DELETE | `/api/v1/data-sources/{id}`                                         | Delete + close pool  |
| GET    | `/api/v1/data-sources/{id}/schema`                                  | Introspect schema    |
| PUT    | `/api/v1/data-sources/{id}/selected-tables`                         | Save selected tables |
| GET    | `/api/v1/data-sources/{id}/tables/{tableName}/data?page=0&size=100` | On-demand data read  |

---

## 6. Security Considerations

- **Tenant data isolation**: every Mongo query is scoped by `tenantId`
  (`findByIdAndTenantId`, not `findById`). No endpoint may return or act on
  a data source belonging to another tenant.
- **Pool isolation**: pool cache key is `{tenantId}:{dataSourceId}`, not just
  `dataSourceId`. This is defense-in-depth (isolation survives even an ID
  collision or bug), gives clean per-tenant metrics/logging, and contains
  the blast radius if one tenant's pool misbehaves.
- **No implicit tenant context**: `tenantId` is passed explicitly on every
  call. No ThreadLocal or global state — deliberately, to avoid leakage
  across pooled threads and to keep the code easy to trace and test.
- **Credential handling**: encrypted at rest, decrypted only transiently in
  memory, never logged, never returned in API responses. Only host + database
  name are logged — never credentials.
- **SQL injection**: table names come from user input and cannot be
  parameterized like values, so every `readTable` call must validate the
  requested `tableName` against the data source's `selectedTables`
  allow-list before it touches a query string. Column values still go
  through `PreparedStatement` parameters as usual.
- **Trust boundary**: this service trusts `X-Tenant-Id` as delivered by the
  Gateway; it does not perform authentication itself. (Open question below.)

---

## 7. Alternatives Considered

| Decision                              | Alternative                                           | Why rejected (for now)                                                                                                                          |
| ------------------------------------- | ----------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------- |
| Enum-based `ConnectorType`            | String-based type field                               | Loses compile-time safety; typos fail silently at runtime instead of at compile time                                                            |
| Per-tenant+data-source pool keying    | Single shared pool with row-level tenant filtering    | Weaker isolation, harder to reason about blast radius, no natural per-tenant metrics                                                            |
| Explicit `tenantId` parameter passing | ThreadLocal tenant context                            | Simpler and safer at this stage; ThreadLocal leakage across pooled threads is a known failure mode we'd rather avoid until it's actually needed |
| Streaming `ResultSet` row-by-row      | Loading full result set into memory before pagination | Avoids OOM risk on large tables since we don't store or cache anything                                                                          |

---

## 8. Open Questions

- What happens if a tenant deletes a table from their database that is still
  listed in `selectedTables`? Should introspection reconcile this
  automatically, or should the read endpoint simply fail per-request?
- What is the Gateway's guarantee around `X-Tenant-Id` — can this service
  ever be reached without going through the Gateway, and if so, is that a
  gap we should flag now even though auth is out of scope for this task?
- How should `information_schema` introspection behave on a very large or
  slow customer database — do we need a timeout/circuit breaker in Phase 1,
  or is that deferred?

---

## 9. Rollout Plan

| Day | Focus                                                                                                                                          |
| --- | ---------------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | Project skeleton, Mongo entities, repositories, basic CRUD (no connection logic)                                                               |
| 2   | `DataConnector`, `MySqlConnector`, `ConnectorFactory`, test-connection endpoint, Jasypt encryption — **code review checkpoint after this day** |
| 3   | `ConnectionPoolManager` with HikariCP, pool cache keyed by tenant+data source, schema introspection                                            |
| 4   | On-demand data read endpoint with pagination, connection stats recording                                                                       |
| 5   | Stats endpoint, tests, README, Postman, demo prep                                                                                              |

---

## 10. Acceptance Criteria

- [ ] All 9 endpoints work; delivered with a Postman collection
- [ ] Bad credentials return a clear 400 and don't save
- [ ] Passwords are AES-encrypted in MongoDB (verified in Compass)
- [ ] Passwords never appear in responses or logs
- [ ] Two tenants cannot see each other's data sources
- [ ] Each data source has its own pool keyed by `{tenantId}:{dataSourceId}`
- [ ] Deleting a data source closes and removes its pool
- [ ] On-demand data reads work with pagination and return rows directly
- [ ] Every connection acquisition and query is recorded in `connection_stats` with pool stats
- [ ] No customer data is persisted
- [ ] Adding a new connector type does not require modifying `ConnectorFactory`
- [ ] Service-layer unit tests (happy path minimum)
- [ ] README explains how to run locally

---

## 11. Deliverables

- Merged PR to `develop`
- Postman collection in `docs/postman/`
- Short README (`docs/data-source-service.md`)
- Demo: register a MySQL data source, read data on demand, show connection
  stats in Compass
