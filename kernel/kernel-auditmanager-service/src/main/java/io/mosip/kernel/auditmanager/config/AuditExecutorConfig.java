package io.mosip.kernel.auditmanager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
/**
 * <h1>Audit Executor Configuration</h1>
 *
 * <p>Configurable ThreadPoolTaskExecutor for asynchronous audit tasks.</p>
 *
 * <h2>Property Mapping:</h2>
 * <ul>
 *   <li><b>audit.executor.core-pool-size</b> – minimum worker threads</li>
 *   <li><b>audit.executor.max-pool-size</b> – burst limit of worker threads</li>
 *   <li><b>audit.executor.queue-capacity</b> – pending task queue size</li>
 *   <li><b>audit.executor.keep-alive-seconds</b> – idle time before non-core threads die</li>
 *   <li><b>audit.executor.await-termination-seconds</b> – graceful shutdown wait</li>
 *   <li><b>audit.executor.thread-name-prefix</b> – thread name prefix</li>
 * </ul>
 */
@Configuration
@EnableAsync(proxyTargetClass = true) // force CGLIB; avoids JDK-proxy gotchas
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
        // Back-pressure: caller runs when saturated (prevents silent drops)
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
