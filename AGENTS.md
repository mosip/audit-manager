# AGENTS.md

This file provides guidance to AI agents when working with code in this repository.

## What This Service Does

`audit-manager` is a MOSIP kernel microservice that provides a centralised, tamper-evident audit trail for the entire MOSIP identity platform. Every other MOSIP service (Registration Processor, ID Authentication, Resident Services, Pre-Registration, etc.) calls this service's single POST endpoint to persist an audit event. The service performs no business logic beyond validating the incoming record and writing it to the `audit.app_audit_log` PostgreSQL table.

## Build & Test Commands

All Maven commands must be run from the `kernel/` directory (the Maven multi-module root).

```bash
# Full build, skip tests and GPG signing (standard dev build)
cd kernel && mvn install -DskipTests=true -Dmaven.javadoc.skip=true -Dgpg.skip=true

# Build and run all tests
cd kernel && mvn install -Dmaven.javadoc.skip=true -Dgpg.skip=true

# Run tests for a single module
cd kernel && mvn test -pl kernel-auditmanager-service -Dgpg.skip=true

# Run a single test class
cd kernel && mvn test -pl kernel-auditmanager-service -Dtest=AuditServiceTest -Dgpg.skip=true

# Generate OpenAPI JSON (starts service on port 8090, generates JSON, then stops)
cd kernel && mvn verify -pl kernel-auditmanager-service -P openapi-doc-generate-profile -Dgpg.skip=true

# SonarQube analysis
cd kernel && mvn verify -P sonar -Dsonar.login=<token> -Dgpg.skip=true
```

Tests use H2 in-memory with schema `AUDIT` auto-created at startup. No external dependencies needed to run tests.

## Module Structure

The project is a two-module Maven build under `kernel/`:

| Module | Purpose |
|--------|---------|
| `kernel-auditmanager-api` | Shared library: JPA entity, repository, request DTO, validation logic (`AuditUtils`), `AuditHandlerImpl`, and builder |
| `kernel-auditmanager-service` | Runnable Spring Boot service: REST controller, service layer, auth wiring, exception handler, Swagger config |

`kernel-auditmanager-service` depends on `kernel-auditmanager-api` as a runtime dependency. External MOSIP services that want to log audits programmatically (not via HTTP) can embed `kernel-auditmanager-api` as a library dependency.

## Request Flow

```
POST /v1/auditmanager/audits
  → AuditManagerController          (service module)
  → AuditManagerServiceImpl         (service module)
  → AuditHandlerImpl.addAudit()     (api module)
      → AuditUtils.validateAudit()  (validates all fields, throws KER-AUD-001 on failure)
      → AuditRepository.save()      (JPA → audit.app_audit_log)
  ← ResponseWrapper<AuditResponseDto> { status: true }
```

The controller wraps requests in MOSIP's `RequestWrapper<AuditRequestDto>` and responses in `ResponseWrapper<AuditResponseDto>`. The response body's `status` field is always `true` on success.

## Database

- **Schema / table:** `audit.app_audit_log`
- **Primary key:** `log_id` (UUID, auto-generated)
- **DDL strategy:** `spring.jpa.hibernate.ddl-auto=none` — schema is managed by scripts in `db_scripts/mosip_audit/` and upgrade scripts in `db_upgrade_scripts/`
- **Production:** PostgreSQL; connection URL injected via `${audit_database_url}` at runtime from Spring Cloud Config
- **Local dev:** H2 in-memory (`application-local.properties`); H2 console available at `/admin/h2-console/`
- **Tests:** H2 in-memory, schema auto-created via `INIT=CREATE SCHEMA IF NOT EXISTS AUDIT` in the JDBC URL; `schema.sql` in main resources initialises the table for local/dev profiles

## Configuration & Profiles

Spring Cloud Config is the source of truth for production config (external `mosip-config` repo, files `application-default.properties` and `kernel-default.properties`). The service name is `kernel-auditmanager-service`.

Local profiles in `kernel/kernel-auditmanager-service/src/main/resources/`:

| Profile | DB | Config server |
|---------|-----|---------------|
| `dev` | PostgreSQL at `dev.mosip.net:30090` | disabled (`spring.cloud.config.enabled=false`) |
| `local` | H2 in-memory | disabled |

Active profile is set in `bootstrap.properties` (`spring.profiles.active=dev`) or overridden at runtime via the `active_profile_env` Docker env var.

## Authentication & Authorisation

The service uses `kernel-auth-adapter` (loaded at runtime via `iam_adapter_url_env` in Docker) to validate MOSIP Bearer tokens. The adapter is added to the classpath via `-Dloader.path`.

Roles permitted to call `POST /audits` are configured via:
```properties
mosip.role.auditmanager.postaudits=INDIVIDUAL,REGISTRATION_ADMIN,REGISTRATION_ADMIN_GENERAL,REGISTRATION_ADMIN_ALL_INDIVIDUAL
```
These roles are bound in `AuthorizedRolesDTO` and injected into the security config.

## Docker & Deployment

The container runs as user `mosip` (UID 1001) on port `8081`. The servlet context path is `/v1/auditmanager`.

Runtime environment variables expected by the container:

| Variable | Purpose |
|----------|---------|
| `active_profile_env` | Spring active profile |
| `spring_config_label_env` | Git branch for Spring Cloud Config |
| `spring_config_url_env` | Spring Cloud Config server URL |
| `iam_adapter_url_env` | URL to download `kernel-auth-adapter.jar` |
| `loader_path_env` | Directory for additional JARs (auth adapter) |

JVM flags are hardcoded in the Dockerfile `CMD`: ZGC with generational mode (`-XX:+UseZGC -XX:+ZGenerational`). Additional JVM tuning is provided via the Helm value `additionalResources.javaOpts` (passed as `JDK_JAVA_OPTIONS`) with a full ZGC tuning profile (heap `-Xms1875m -Xmx3200m`, metaspace limits, GC thread counts, etc.) — see `helm/auditmanager/values.yaml`.

Helm chart is in `helm/auditmanager/`. Install via `deploy/install.sh [kubeconfig]` which deploys into the `kernel` namespace with Istio injection enabled. Chart version for develop is `0.0.1-develop`.

## CI/CD

GitHub Actions (`.github/workflows/push-trigger.yml`) uses reusable workflows from `mosip/kattu@master-java21`:
1. **build-maven-audit-manager** — Maven build at `./kernel/`
2. **publish_to_nexus** — publishes SNAPSHOT artifacts (skipped on master, PRs, and releases)
3. **build-dockers** — builds and pushes `mosipqa/kernel-auditmanager-service` image
4. **sonar_analysis** — SonarQube analysis via sonarcloud.io (skipped on PRs)

Triggers: pushes to `MOSIP*`, `develop*`, `master`, `1.*`, `release*` branches.

## Version Management

The project version follows `kernel/pom.xml`. All three pom files (`kernel/pom.xml`, `kernel-auditmanager-api/pom.xml`, `kernel-auditmanager-service/pom.xml`) must carry the same version. Internal dependency version properties (`kernel.core.version`, `kernel.audit-api.version`, etc.) are also kept in sync. When bumping versions, update all occurrences of the SNAPSHOT string across all three files.
