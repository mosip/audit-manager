FROM eclipse-temurin:21-jre-alpine

# can be passed during Docker build as build time environment for github branch to pickup configuration from.
ARG spring_config_label

# can be passed during Docker build as build time environment for spring profiles active 
ARG active_profile

# can be passed during Docker build as build time environment for config server URL 
ARG spring_config_url

# can be passed during Docker build as build time environment for glowroot
ARG is_glowroot

# can be passed during Docker build as build time environment for artifactory URL
ARG artifactory_url

ARG loader_path

# can be passed during Docker build as build time environment for github branch to pickup configuration from.
ARG container_user=mosip
ARG container_user_group=mosip
ARG container_user_uid=1001
ARG container_user_gid=1001

# can be passed during Docker build as build time environment for label related addition to docker.
ARG SOURCE
ARG COMMIT_HASH
ARG COMMIT_ID
ARG BUILD_TIME

# environment variable to pass active profile such as DEV, QA etc at docker runtime
ENV active_profile_env=${active_profile}

# environment variable to pass github branch to pickup configuration from, at docker runtime
ENV spring_config_label_env=${spring_config_label}

# environment variable to pass spring configuration url, at docker runtime
ENV spring_config_url_env=${spring_config_url}

# environment variable to pass glowroot, at docker runtime
ENV is_glowroot_env=${is_glowroot}

# environment variable to pass artifactory url, at docker runtime
ENV artifactory_url_env=${artifactory_url}

# environment variable to pass iam_adapter url, at docker runtime
ENV iam_adapter_url_env=${iam_adapter_url}

# environment variable to pass to set workdir.
ENV work_dir=/home/${container_user}

# environment variable to pass for setting loader path
ENV loader_path_env=${work_dir}/additional_jars/

# Labels can be added to the image for metadata purposes such as source, commit hash, commit ID, and build time.
LABEL source=${SOURCE}
LABEL commit_hash=${COMMIT_HASH}
LABEL commit_id=${COMMIT_ID}
LABEL build_time=${BUILD_TIME}

## install packages, create user and change permissions of file inside working dir
RUN apk -q update \
&& apk add -q unzip wget \
&& addgroup -g ${container_user_gid} ${container_user_group} \
&& adduser -s /bin/sh -u ${container_user_uid} -G ${container_user_group} -h /home/${container_user} --disabled-password ${container_user} \
&& mkdir -p ${loader_path_env} \
&& chown -R ${container_user}:${container_user} /home/${container_user}

#copy the jar to container
COPY ./target/kernel-auditmanager-service-*.jar /home/${container_user}/kernel-auditmanager-service.jar

# set working directory for the user
WORKDIR /home/${container_user}

# select container user for all tasks
USER ${container_user_uid}:${container_user_gid}

# can expose required port
EXPOSE 8081

# the command to run when the container starts
CMD  wget -q --show-progress "${iam_adapter_url_env}" -O "${loader_path_env}"/kernel-auth-adapter.jar; \
    java -XX:+ExplicitGCInvokesConcurrent -XX:+UseZGC -XX:+ZGenerational -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions -XX:+UseStringDeduplication -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8 -XX:+UseCompressedOops \
	 -Dloader.path="${loader_path_env}" -Dspring.cloud.config.label="${spring_config_label_env}" -Dspring.profiles.active="${active_profile_env}" -Dspring.cloud.config.uri="${spring_config_url_env}" -jar kernel-auditmanager-service.jar ; \
