[![Maven Package upon a push](https://github.com/mosip/audit-manager/actions/workflows/push_trigger.yml/badge.svg?branch=develop)](https://github.com/mosip/audit-manager/actions/workflows/push_trigger.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=mosip_audit-manager&metric=alert_status)](https://sonarcloud.io/dashboard?branch=develop&id=mosip_audit-manager)

# Audit Manager

Centralised, tamper-evident audit trail for the MOSIP identity platform.
Other services call a single HTTP endpoint to persist events into `audit.app_audit_log`.

| | |
|---|---|
| **JDK** | 21 |
| **Spring Boot** | 4.1.1 (**no** `kernel-bom`) |
| **Spring Cloud** | 2025.1.3 |
| **Module version** | `1.4.1-SNAPSHOT` |
| **HTTP port / context** | `8081` / `/v1/auditmanager` |
| **License** | [MPL 2.0](LICENSE) — see [NOTICE](NOTICE) |

**Quick links (after `run-local … start`):**

| Purpose | URL |
|---------|-----|
| Health | http://localhost:8081/v1/auditmanager/actuator/health |
| Swagger UI | http://localhost:8081/v1/auditmanager/swagger-ui/index.html |
| OpenAPI | http://localhost:8081/v1/auditmanager/v3/api-docs |
| Audits API | `POST` http://localhost:8081/v1/auditmanager/audits |

---

## Modules

| Module | Role |
|--------|------|
| [`kernel-auditmanager-api`](kernel/kernel-auditmanager-api/) | Embeddable library: entity, repository, `AuditUtils`, `AuditHandlerImpl`, builder |
| [`kernel-auditmanager-service`](kernel/kernel-auditmanager-service/) | Runnable Spring Boot service (fat ZIP JAR) |

```
POST /v1/auditmanager/audits
  → Controller → ServiceImpl → AuditHandlerImpl
      → AuditUtils.validate*  → KER-AUD-001 on failure
      → AuditRepository.save  → audit.app_audit_log
  ← ResponseWrapper { status: true }
```

---

## Prerequisites

- **JDK 21**, **Maven 3.9+**
- **Docker** (optional — for `run-local … docker`)
- Local SNAPSHOTs (or your snapshot repo), aligned with **commons** / **bio-utils** `1.4.1-SNAPSHOT`:
  - `commons/kernel` → `kernel-core`, `kernel-auth-adapter`
- Config (production): [mosip-config](https://github.com/mosip/mosip-config)

---

## Build

```bash
cd kernel
mvn clean install "-Dgpg.skip=true"
```

| Goal | Command |
|------|---------|
| Skip tests | `mvn clean install "-DskipTests=true" "-Dgpg.skip=true"` |
| One module | `mvn test -pl kernel-auditmanager-service "-Dgpg.skip=true"` |
| One test class | `mvn test -pl kernel-auditmanager-service "-Dtest=AuditServiceTest" "-Dgpg.skip=true"` |
| Coverage | `*/target/site/jacoco/index.html` (JaCoCo gate **100%** on non-excluded packages) |
| OpenAPI JSON | `mvn verify -pl kernel-auditmanager-service -P openapi-doc-generate-profile "-Dgpg.skip=true"` |

---

## Local setup

Helpers live next to the runnable module (same style as [`kernel-bio-converter`](https://github.com/mosip/converters)):

- Windows cmd: `kernel/kernel-auditmanager-service/run-local.bat`
- Linux / macOS / Git Bash: `kernel/kernel-auditmanager-service/run-local.sh`

Working directory must be **`kernel/kernel-auditmanager-service/`**. Logs and PID: `.local/` (gitignored).

### Commands

| Command | What it does |
|---------|----------------|
| `init` | Maven package this module (skip tests) |
| `start` | Start on port **8081**, wait until ready, print URLs |
| `smoke` | `GET …/actuator/health` and Swagger UI |
| `stop` | Stop the process |
| `test` | Maven unit tests |
| `all` | `init` + `test` + `start` + `smoke` |
| `docker` | `init`, docker build, run |
| _(no args)_ / `help` | Usage + endpoint URLs |

Default profile is **`local`** (H2 in-memory). `start` uses Generational ZGC and MOSIP `--add-opens` / `--enable-preview`.

### Windows (cmd)

```bat
cd kernel\kernel-auditmanager-service
run-local.bat init
run-local.bat start
run-local.bat smoke
```

```bat
set PORT=8081
set SPRING_PROFILES_ACTIVE=local
set SPRING_CLOUD_CONFIG_URI=http://localhost:51000
set SPRING_CLOUD_CONFIG_LABEL=master
set loader_path_env=.
run-local.bat start
```

### Linux / macOS / Git Bash

```bash
cd kernel/kernel-auditmanager-service
chmod +x run-local.sh
./run-local.sh init
./run-local.sh start
./run-local.sh smoke
```

### Environment variables

| Variable | Default | Meaning |
|----------|---------|---------|
| `PORT` | `8081` | HTTP port |
| `SPRING_PROFILES_ACTIVE` | `local` | Spring profile (`local` = H2) |
| `SPRING_CLOUD_CONFIG_URI` | _(empty)_ | Config-server URI |
| `SPRING_CLOUD_CONFIG_LABEL` | _(empty)_ | Config-server label / branch |
| `loader_path_env` | `.` | Extra JARs (`-Dloader.path`, e.g. auth-adapter) |
| `IMAGE` | `kernel-auditmanager-service` | Docker image name |
| `JDK_JAVA_OPTIONS` | _(empty)_ | Extra JVM options for `docker` runs |

---

## Configuration

Production config is served by Spring Cloud Config (`kernel-auditmanager-service`):

1. [application-default.properties](https://github.com/mosip/mosip-config/blob/master/application-default.properties)
2. [kernel-default.properties](https://github.com/mosip/mosip-config/blob/master/kernel-default.properties)

Local profiles under `kernel/kernel-auditmanager-service/src/main/resources/`:

| Profile | DB | Config server |
|---------|-----|---------------|
| `local` | H2 in-memory | disabled |
| `dev` | PostgreSQL (see `application-dev.properties`) | disabled |

---

## Database

- Schema / table: `audit.app_audit_log`
- DDL: `spring.jpa.hibernate.ddl-auto=none` in prod — scripts in [`db_scripts/mosip_audit/`](db_scripts/)
- Upgrades: [`db_upgrade_scripts/`](db_upgrade_scripts/)

---

## Docker & deploy

```bash
cd kernel/kernel-auditmanager-service
docker build -t kernel-auditmanager-service .
# or: run-local.bat docker / ./run-local.sh docker
```

Helm: [`helm/auditmanager/`](helm/auditmanager/). Install helper: [`deploy/install.sh`](deploy/).

Runtime env (container): `active_profile_env`, `spring_config_label_env`, `spring_config_url_env`, `iam_adapter_url_env`, `loader_path_env`.

---

## API

`POST /audits` with MOSIP `RequestWrapper<AuditRequestDto>`. Success body: `ResponseWrapper` with `status: true`.

Roles (when adapter / `@PreAuthorize` enabled): configured via `mosip.role.auditmanager.postaudits`.

Functional tests: [mosip-functional-tests](https://github.com/mosip/mosip-functional-tests).

---

## Notices & licenses

Third-party attributions and the MOSIP license matrix: [NOTICE](NOTICE). Full texts: [`licenses/`](licenses/).

---

## License

[Mozilla Public License 2.0](LICENSE)
