FROM openjdk:11

ARG SOURCE
ARG COMMIT_HASH
ARG COMMIT_ID
ARG BUILD_TIME
LABEL source=${SOURCE}
LABEL commit_hash=${COMMIT_HASH}
LABEL commit_id=${COMMIT_ID}
LABEL build_time=${BUILD_TIME}

# Environment variables for build-time configuration
ARG spring_config_label
ARG active_profile
ARG spring_config_url
ARG is_glowroot
ARG artifactory_url
ARG container_user=mosip
ARG container_user_group=mosip
ARG container_user_uid=1001
ARG container_user_gid=1001
ARG loader_path=/home/${container_user}/additional_jars/

# Set working directory and copy application jar
WORKDIR /home/${container_user}
COPY ./target/kernel-auditmanager-service-*.jar kernel-auditmanager-service.jar

# Change permissions of files
RUN apt-get -y update \
    && apt-get install -y unzip \
    && groupadd -g ${container_user_gid} ${container_user_group} \
    && useradd -u ${container_user_uid} -g ${container_user_group} -s /bin/sh -m ${container_user} \
    && mkdir -p ${loader_path} \
    && chown -R ${container_user}:${container_user} /home/${container_user}

# Environment variables for runtime configuration
ENV active_profile_env=${active_profile}
ENV spring_config_label_env=${spring_config_label}
ENV spring_config_url_env=${spring_config_url}
ENV is_glowroot_env=${is_glowroot}
ENV artifactory_url_env=${artifactory_url}
ENV loader_path_env=${loader_path}

# Expose port
EXPOSE 8081

# Select container user for all tasks
USER ${container_user_uid}:${container_user_gid}

# Download additional dependencies and run the application
CMD if [ "$is_glowroot_env" = "present" ]; then \
        wget -q --show-progress "${artifactory_url_env}"/artifactory/libs-release-local/io/mosip/testing/glowroot.zip && \
        unzip glowroot.zip && \
        rm -rf glowroot.zip && \
        sed -i 's/<service_name>/kernel-auditmanager-service/g' glowroot/glowroot.properties ; \
    fi && \
    wget -q --show-progress "${iam_adapter_url_env}" -O "${loader_path_env}"/kernel-auth-adapter.jar && \
    java -jar ${loader_path_env}/kernel-auth-adapter.jar && \
    java -jar -javaagent:glowroot/glowroot.jar -Dloader.path="${loader_path_env}" -jar -Dspring.cloud.config.label="${spring_config_label_env}" -Dspring.profiles.active="${active_profile_env}"  -Dspring.cloud.config.uri="${spring_config_url_env}" kernel-auditmanager-service.jar
