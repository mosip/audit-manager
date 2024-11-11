# Kernel Auditmanager Service

## Overview
This service is used to persist audits.

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

## Configuration files
Kernel Audit Manger Service uses the following configuration files that are accessible in this [repository](https://github.com/mosip/mosip-config/tree/master).
```
application-default.properties
kernel-default.properties
```

## Configuration
[Configuration-Application](https://github.com/mosip/mosip-config/blob/master/application-default.properties) and
[Configuration-Kernel](https://github.com/mosip/mosip-config/blob/master/kernel-default.properties) defined here.

## Deploy
To deploy service on Kubernetes cluster using Dockers refer to [Sandbox Deployment](https://docs.mosip.io/1.2.0/deploymentnew/v3-installation).

## Test
Automated functional tests available in [Functional Tests repo](https://github.com/mosip/mosip-functional-tests).

## APIs
API documentation is available [here](https://mosip.github.io/documentation/1.2.0/kernel-auditmanager-service.html).

## Default context-path and port
Refer [`bootstrap.properties`](src/main/resources/bootstrap.properties)