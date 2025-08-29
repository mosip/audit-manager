# Auditmanager

Helm chart for installing Kernel module Auditmanager.

## TL;DR

```console
$ helm repo add mosip https://mosip.github.io
$ helm install my-release mosip/auditmanager
```

## Introduction

Auditmanager is  part of the kernel modules, but has a separate Helm chart so as to install and manage it in a completely indepedent namespace.

## Prerequisites

- Kubernetes 1.12+
- Helm 3.1.0
- PV provisioner support in the underlying infrastructure
- ReadWriteMany volumes for deployment scaling

## Installing the Chart

To install the chart with the release name `auditmanager`.

```console
helm install my-release mosip/auditmanager
```

### Custom Container User

The chart supports dynamic container user configuration. By default, it uses `mosip` as the container user, but you can customize it:

```console
helm install my-release mosip/auditmanager \
  --set containerSecurityContext.runAsUser=myuser \
  --set auditLogsPersistence.enabled=true
```

This will:
- Create audit logs directory at `/var/log/myuser/audit` inside the container
- Set the WAL file path to `/var/log/myuser/audit/audit-wal-{POD_NAME}.log`
- Configure persistent storage for audit logs

**Note**: When using a custom container user, ensure your Docker image is built with the same user:
```bash
docker build --build-arg container_user=myuser -t your-image .
```

> **Tip**: List all releases using `helm list`

## Uninstalling the Chart

To uninstall/delete the `my-release` deployment:

```console
helm delete my-release
```

