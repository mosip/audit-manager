FROM eclipse-temurin:21-jre-alpine

# Labels assigned at Docker build time
ARG SOURCE
ARG COMMIT_HASH
ARG COMMIT_ID
ARG BUILD_TIME
LABEL source=${SOURCE}
LABEL commit_hash=${COMMIT_HASH}
LABEL commit_id=${COMMIT_ID}
LABEL build_time=${BUILD_TIME}

# Build-time defaults (overridable at runtime via *_env)
ARG spring_config_label=master
ARG active_profile=default
ARG spring_config_url

ARG container_user=mosip
ARG container_user_group=mosip
ARG container_user_uid=1001
ARG container_user_gid=1001

# Runtime env + workdir. loader.path kept for optional extra jars (auth-adapter is in the fat JAR from Maven).
ENV active_profile_env=${active_profile} \
    spring_config_label_env=${spring_config_label} \
    spring_config_url_env=${spring_config_url} \
    work_dir=/home/${container_user} \
    loader_path_env=/home/${container_user}/additional_jars/

WORKDIR ${work_dir}

# Create user (no wget — kernel-auth-adapter comes from the Maven fat JAR)
RUN apk -q update \
    && apk add -q --no-cache bash \
    && addgroup -g ${container_user_gid} ${container_user_group} \
    && adduser -s /bin/sh -u ${container_user_uid} -G ${container_user_group} -h ${work_dir} --disabled-password ${container_user} \
    && mkdir -p ${loader_path_env} \
    && chown -R ${container_user}:${container_user} ${work_dir} \
    && rm -rf /var/cache/apk/*

# Copy built JARs (fat + optional attachments); destination must end with /
COPY ./target/kernel-auditmanager-service-*.jar ${work_dir}/

# Keep Boot fat JAR only; drop -sources / -javadoc / .original
RUN set -eux; \
    cd ${work_dir}; \
    rm -f kernel-auditmanager-service-*-sources.jar \
          kernel-auditmanager-service-*-javadoc.jar \
          kernel-auditmanager-service-*.jar.original \
          kernel-auditmanager-service.jar.original 2>/dev/null || true; \
    fat=""; \
    for f in kernel-auditmanager-service-*.jar; do \
      [ -f "$f" ] || continue; \
      case "$f" in \
        *-sources.jar|*-javadoc.jar) rm -f "$f" ;; \
        *) fat="$f" ;; \
      esac; \
    done; \
    if [ -z "$fat" ] || [ ! -f "$fat" ]; then \
      echo "error: Boot fat jar not found under ${work_dir}" >&2; \
      ls -la "${work_dir}" >&2; \
      exit 1; \
    fi; \
    mv -f "$fat" kernel-auditmanager-service.jar; \
    chown -R ${container_user}:${container_user} ${work_dir}

USER ${container_user_uid}:${container_user_gid}

EXPOSE 8081

# JVM GC / heap: use run-local.* or Helm JDK_JAVA_OPTIONS. Shell form so ${*_env} expands at start.
CMD java -Dfile.encoding=UTF-8 \
    -Dloader.path="${loader_path_env}" \
    -Dspring.cloud.config.label="${spring_config_label_env}" \
    -Dspring.profiles.active="${active_profile_env}" \
    -Dspring.cloud.config.uri="${spring_config_url_env}" \
    -jar kernel-auditmanager-service.jar
