package io.mosip.kernel.auditmanager.queue;

import io.mosip.kernel.auditmanager.entity.Audit;
import io.mosip.kernel.auditmanager.impl.AuditHandlerImpl;
import io.mosip.kernel.auditmanager.repository.AuditRepository;
import io.mosip.kernel.auditmanager.request.AuditRequestDto;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Scheduled batch writer that drains the {@link AuditQueueService} and persists
 * records in bulk using {@code saveAll()}, reducing DB round-trips under load.
 *
 * <p>Retry strategy: exponential back-off (1s, 2s, 4s) up to
 * {@code mosip.kernel.auditmanager.batch.retry-attempts} times before giving up.
 *
 * <p>Graceful shutdown: {@link #flushOnShutdown()} drains remaining queue entries
 * so no audit records are silently lost when the pod terminates.
 *
 * @since 1.3.0
 */
@Component
public class AuditBatchWriter {

    private static final Logger log = LoggerFactory.getLogger(AuditBatchWriter.class);

    @Autowired
    private AuditQueueService queueService;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private AuditHandlerImpl auditHandler;

    @Value("${mosip.kernel.auditmanager.batch.size:100}")
    private int batchSize;

    @Value("${mosip.kernel.auditmanager.batch.retry-attempts:3}")
    private int retryAttempts;

    /**
     * Runs every {@code flush-interval-ms} milliseconds (default 100ms).
     * Uses {@code fixedDelay} so the next run starts only after the current one
     * finishes, preventing concurrent writes to the DB.
     */
    @Scheduled(fixedDelayString = "${mosip.kernel.auditmanager.batch.flush-interval-ms:100}")
    public void flushBatch() {
        List<AuditRequestDto> batch = new ArrayList<>(batchSize);
        int drained = queueService.drainTo(batch, batchSize);
        if (drained == 0) {
            return;
        }
        writeBatchWithRetry(batch);
    }

    /**
     * Called by the JVM shutdown hook (via Spring context close).
     * Flushes whatever remains in the queue so in-flight audits are not lost.
     */
    @PreDestroy
    public void flushOnShutdown() {
        log.info("Shutdown signal received — flushing remaining audit queue entries...");
        List<AuditRequestDto> remaining = new ArrayList<>();
        queueService.drainTo(remaining, Integer.MAX_VALUE);
        if (!remaining.isEmpty()) {
            log.info("Writing {} remaining audit records before shutdown", remaining.size());
            writeBatchWithRetry(remaining);
        }
    }

    private void writeBatchWithRetry(List<AuditRequestDto> batch) {
        for (int attempt = 0; attempt < retryAttempts; attempt++) {
            try {
                List<Audit> entities = batch.stream()
                        .map(auditHandler::getAuditEntity)
                        .collect(Collectors.toList());
                auditRepository.saveAll(entities);
                log.debug("Persisted batch of {} audit records", entities.size());
                return;
            } catch (DataAccessException ex) {
                if (attempt < retryAttempts - 1) {
                    long backoffMs = (long) Math.pow(2, attempt) * 1000;
                    log.warn("Batch write failed (attempt {}/{}), retrying in {}ms: {}",
                            attempt + 1, retryAttempts, backoffMs, ex.getMessage());
                    try {
                        Thread.sleep(backoffMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("Retry interrupted — {} audit records may be lost", batch.size());
                        return;
                    }
                } else {
                    log.error("Batch write failed after {} attempts — {} audit records lost",
                            retryAttempts, batch.size(), ex);
                }
            }
        }
    }
}
