# kernel/

```
JDK21 · Maven3.9+ · Boot4.1.1 · Cloud2025.1.3 · jackson2.bom 2.22.3 · MOSIP 1.4.1-SNAPSHOT
└─ mvn clean install "-Dgpg.skip=true"
```

```
reactor
├─ kernel-auditmanager-api       # entity · repo · AuditUtils · AuditHandlerImpl · builder
│  └─ no Boot repackage
└─ kernel-auditmanager-service   # REST · Boot ZIP · springdoc · actuator · auth-adapter
   ├─ :8081 /v1/auditmanager
   ├─ run-local.bat|sh → init|start|smoke|stop|test|all|docker
   ├─ Swagger: relative server=${server.servlet.path} · Authorize=Authorization apiKey
   └─ Dockerfile · auth-adapter in fat JAR (no configure_start / iam_adapter wget)
```

```
flow
POST /audits → Controller → ServiceImpl → AuditHandlerImpl
├─ AuditUtils.validate* → KER-AUD-001
└─ AuditRepository.save → audit.app_audit_log
```

```
rules
├─ siblings omit <version> (DM in parent)
├─ spring-boot-jackson2 · ban kernel-bom · ban kernel-logger-logback
├─ security: auth-adapter in fat JAR · TestSecurityConfig permitAll (tests)
├─ db: prod=PostgreSQL ddl-auto=none · local|test=H2 schema AUDIT
└─ jacoco=100% excl constant/config/dto/entity/exception/repository/*BootApplication
```
