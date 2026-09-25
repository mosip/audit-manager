package io.mosip.kernel.auditmanager.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Configurable {@link ThreadPoolTaskExecutor} for asynchronous audit tasks.
 *
 * <p>Properties: {@code audit.executor.core-pool-size}, {@code max-pool-size},
 * {@code queue-capacity}, {@code keep-alive-seconds}, {@code await-termination-seconds},
 * {@code thread-name-prefix}.</p>
 */
@Configuration
@EnableAsync(proxyTargetClass = true)
public class AuditExecutorConfig {

	@Value("${audit.executor.core-pool-size:8}")
	private int corePoolSize;

	@Value("${audit.executor.max-pool-size:12}")
	private int maxPoolSize;

	@Value("${audit.executor.queue-capacity:500}")
	private int queueCapacity;

	@Value("${audit.executor.keep-alive-seconds:60}")
	private int keepAliveSeconds;

	@Value("${audit.executor.await-termination-seconds:30}")
	private int awaitTerminationSeconds;

	@Value("${audit.executor.thread-name-prefix:Audit-Async-}")
	private String threadNamePrefix;

	@Bean(name = "auditExecutor")
	public Executor auditExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(corePoolSize);
		executor.setMaxPoolSize(maxPoolSize);
		executor.setQueueCapacity(queueCapacity);
		executor.setThreadNamePrefix(threadNamePrefix);
		executor.setAllowCoreThreadTimeOut(true);
		executor.setKeepAliveSeconds(keepAliveSeconds);
		executor.setAwaitTerminationSeconds(awaitTerminationSeconds);
		executor.setWaitForTasksToCompleteOnShutdown(true);
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		executor.initialize();
		return executor;
	}
}
