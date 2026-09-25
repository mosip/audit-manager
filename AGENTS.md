# audit-manager

Audit trail · **JDK21 · Boot4.1.1 · Cloud2025.1.3 · no kernel-bom.** Load only the nested `AGENTS.md` for the folder you edit.

```
audit-manager/
├── kernel/                    # reactor — see kernel/AGENTS.md
│   ├── kernel-auditmanager-api/
│   └── kernel-auditmanager-service/   # :8081 /v1/auditmanager · run-local.* · Dockerfile
├── db_scripts/mosip_audit/    # PostgreSQL DDL
├── db_upgrade_scripts/
├── helm/auditmanager/         # chart
├── deploy/                    # install.sh · NS=kernel
├── licenses/ · NOTICE · LICENSE
└── README.md                  # how-to (prefer over expanding this file)
```

**Prereq:** `../commons/kernel` → `kernel-core` · `kernel-auth-adapter` **1.4.1-SNAPSHOT**

**Build/run:** `cd kernel && mvn clean install "-Dgpg.skip=true"` · `run-local.bat|sh` `init|start|smoke|stop|test|all|docker`

**Rules:** one folder · Grep/Glob · skip `target/` · pins **1.4.1-SNAPSHOT** · no `kernel-bom`/`kernel-logger-logback` · Jackson2 via `jackson2.bom.version`(**2.22.3**) never Boot `jackson-bom.version` · freeze `POST /v1/auditmanager/audits` · JaCoCo**=100%** (excl constant/config/dto/entity/exception/repository/*BootApplication) · GC flags only in `run-local.*` not Dockerfile
