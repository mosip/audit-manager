# Auditmanager 

## Overview
Refer [Commons](https://docs.mosip.io/1.2.0/modules/commons).

## Features
- **Persistent Storage**: Optional persistent storage for audit logs including WAL (Write-Ahead Log) files
- **Dynamic User Configuration**: Configurable container user with automatic path adjustment
- **Volume Permissions**: Optional init container for proper file permissions
- **SSL Configuration**: Support for both secure and insecure SSL modes
- **Interactive & Non-Interactive**: Support for both interactive prompts and environment variables

## Install 

### Interactive Mode
```bash
./install.sh
```
The script will prompt you for various configuration options.

### Non-Interactive Mode
```bash
# Example with persistent storage
export ENABLE_PERSISTENT_STORAGE=true
export STORAGE_SIZE=2Gi
export ENABLE_VOLUME_PERMISSIONS=true
./install.sh
```

See [INSTALLATION_EXAMPLES.md](INSTALLATION_EXAMPLES.md) for more examples.

## SSL Configuration
* During the execution of the `install.sh` script, a prompt appears requesting information regarding the presence of a public domain and a valid SSL certificate on the server.
* If the server lacks a public domain and a valid SSL certificate, it is advisable to select the `n` option. Opting it will enable the `init-container` with an `emptyDir` volume and include it in the deployment process.
* The init-container will proceed to download the server's self-signed SSL certificate and mount it to the specified location within the container's Java keystore (i.e., `cacerts`) file.
* This particular functionality caters to scenarios where the script needs to be employed on a server utilizing self-signed SSL certificates.

## Persistent Storage
When enabled, the audit manager will store WAL files persistently at:
```
/var/log/{container_user}/audit/audit-wal-{POD_NAME}.log
```

This integrates with the config server property:
```properties
mosip.kernel.auditmanager.wal-file-path=/var/log/mosip/audit/audit-wal-${POD_NAME}.log
```
