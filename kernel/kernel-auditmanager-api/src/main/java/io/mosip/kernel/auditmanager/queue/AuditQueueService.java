package io.mosip.kernel.auditmanager.queue;

import io.mosip.kernel.auditmanager.request.AuditRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Thread-safe in-memory queue that decouples HTTP request threads from DB writes.
 * Producers call {@link #enqueue} and return immediately; a background batch
 * writer drains this queue on a fixed schedule.
 *
 * @since 1.3.0
 */
@Component
public class AuditQueueService {

    private static final Logger log = LoggerFactory.getLogger(AuditQueueService.class);

    private final LinkedBlockingQueue<AuditRequestDto> queue;
    private final int capacity;

    public AuditQueueService(
            @Value("${mosip.kernel.auditmanager.queue.capacity:50000}") int capacity) {
        this.capacity = capacity;
        this.queue = new LinkedBlockingQueue<>(capacity);
    }

    /**
     * Non-blocking enqueue. Returns {@code false} and logs a warning if the queue
     * is full, allowing callers to degrade gracefully instead of blocking.
     */
    public boolean enqueue(AuditRequestDto auditRequest) {
        boolean accepted = queue.offer(auditRequest);
        if (!accepted) {
            log.warn("Audit queue is full (capacity={}). Dropping audit event: eventId={}",
                    capacity, auditRequest.getEventId());
        }
        return accepted;
    }

    /**
     * Drains up to {@code maxElements} entries into the provided list.
     * Thread-safe; safe to call from a single scheduler thread.
     */
    public int drainTo(List<AuditRequestDto> target, int maxElements) {
        return queue.drainTo(target, maxElements);
    }

    public int size() {
        return queue.size();
    }
}
