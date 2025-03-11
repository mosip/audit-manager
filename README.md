[![Maven Package upon a push](https://github.com/mosip/audit-manager/actions/workflows/push-trigger.yml/badge.svg?branch=release-1.3.x)](https://github.com/mosip/audit-manager/actions/workflows/push-trigger.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=mosip_audit-manager&metric=alert_status)](https://sonarcloud.io/dashboard?branch=release-1.3.x&id=mosip_audit-manager)

# Audit Manager

## Overview
This repository contains modules used for audit related functionalities.

## Databases
Refer to [SQL scripts](db_scripts).

## Build & run (for developers)
The project requires JDK 21.0.3
and mvn version - 3.9.6.
1. Build and install:
    ```
    $ cd kernel
    $ mvn install -DskipTests=true -Dmaven.javadoc.skip=true -Dgpg.skip=true
    ```
2. Build Docker for a service:
    ```
    $ cd <service folder>
    $ docker build -f Dockerfile
    ```

## Configuration
Kernel Audit Manager Service uses the following configuration files that are accessible in this [repository](https://github.com/mosip/mosip-config/tree/master).
Please refer to the required released tagged version for configuration.
1. [Configuration-Application](https://github.com/mosip/mosip-config/blob/master/application-default.properties)
2. [Configuration-Kernel](https://github.com/mosip/mosip-config/blob/master/kernel-default.properties)

## Deploy
To deploy service on Kubernetes cluster using Dockers refer to [Sandbox Deployment](https://docs.mosip.io/1.2.0/deploymentnew/v3-installation).

## Test
Automated functional tests available in [Functional Tests repo](https://github.com/mosip/mosip-functional-tests).

## APIs
API documentation is available [here](https://mosip.github.io/documentation/1.2.0/kernel-auditmanager-service.html).

## License
This project is licensed under the terms of [Mozilla Public License 2.0](LICENSE)
