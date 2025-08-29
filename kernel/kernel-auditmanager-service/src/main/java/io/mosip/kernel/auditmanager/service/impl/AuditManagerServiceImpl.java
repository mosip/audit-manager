package io.mosip.kernel.auditmanager.service.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.auditmanager.dto.AuditResponseDto;
import io.mosip.kernel.auditmanager.repository.AuditRepository;
import io.mosip.kernel.auditmanager.request.AuditRequestDto;
import io.mosip.kernel.auditmanager.service.AuditManagerService;
import io.mosip.kernel.core.auditmanager.spi.AuditHandler;

/**
 * Default implementation of {@link AuditManagerService} that supports:
 * <ul>
 *   <li>Direct (synchronous) audit writes</li>
 *   <li>Buffered, asynchronous audit ingestion using an in-memory buffer</li>
 *   <li>Crash-safe buffering with a local write-ahead log (WAL) file</li>
 *   <li>Periodic scheduled flush of buffered records to the backing store via {@link AuditHandler}</li>
 *   <li>Periodic cleanup of old audit records</li>
 * </ul>
 *
 * <h2>Design &amp; Behavior</h2>
 * <p>
 * The service maintains an in-memory {@link CopyOnWriteArrayList} buffer and a persistent WAL file.
 * Each async ingestion appends to the WAL first (for durability) and then to the buffer.
 * A scheduled task {@link #flushAuditBuffer()} periodically attempts to persist buffered records
 * via {@link AuditHandler#addAudits(List)}, and on success, truncates the WAL.
 * On startup, the service attempts to recover buffered-but-unflushed records by replaying the WAL
 * into memory (see {@link #recoverFromWalFile()}).
 * </p>
 *
 * <h3>Thread-safety</h3>
 * <p>
 * The buffer is a {@link CopyOnWriteArrayList}, which prioritizes read iteration safety.
 * Writes induce array copies; this is acceptable when write bursts are moderate and reads are frequent.
 * If extremely high write rates are expected, consider replacing with a lock-free or queue-based structure.
 * </p>
 *
 * <h3>Transactions</h3>
 * <p>
 * Methods annotated with {@link Transactional} ensure that a single flush to the backing store (via
 * {@link AuditHandler}) participates in a Spring-managed transaction if a transactional store is used.
 * </p>
 *
 * <h3>Configuration</h3>
 * <ul>
 *   <li>{@code mosip.kernel.auditmanager.buffer-size} — max buffer size before a proactive flush is triggered</li>
 *   <li>{@code mosip.kernel.auditmanager.flush-interval-millis} — periodic flush interval in milliseconds</li>
 *   <li>{@code mosip.kernel.auditmanager.wal-file-path} — filesystem path of the WAL file</li>
 * </ul>
 *
 * <h3>Failure Modes</h3>
 * <ul>
 *   <li>If appending to the WAL fails, the record is logged and the method proceeds (best-effort durability).</li>
 *   <li>If a flush fails, buffered records are retained (in-memory) and WAL is not truncated; the next run retries.</li>
 *   <li>Malformed WAL lines are skipped during recovery with error log entries.</li>
 * </ul>
 *
 * @implSpec This implementation delegates persistence to {@link AuditHandler}. Repository is present
 *           for potential future direct DB operations; it is not used in current methods.
 */
@Service
public class AuditManagerServiceImpl implements AuditManagerService {

	private static final Logger logger = LoggerFactory.getLogger(AuditManagerServiceImpl.class);

	private final AuditHandler<AuditRequestDto> auditHandler;
	private final AuditRepository auditRepository; // currently unused; reserved for direct DB enhancements
	private final ObjectMapper objectMapper;

	/** In-memory buffer for async ingestion; paired with crash-safe WAL. */
	private final List<AuditRequestDto> auditBuffer = new CopyOnWriteArrayList<>();

	/** Upper bound for {@link #auditBuffer} length before a proactive flush is triggered. */
	private final int bufferSize;

	/** Periodic flush cadence in milliseconds (also used by @Scheduled). */
	private final long flushIntervalMillis;

	/** Filesystem path of the write-ahead log (WAL) file. */
	private final String walFilePath;

	/** WAL file handle; created if missing on initialization. */
	private File walFile;

	private final long retentionPeriodMillis; // <-- add field

	private final java.util.concurrent.locks.ReentrantLock walLock = new java.util.concurrent.locks.ReentrantLock();
	private final java.util.concurrent.locks.ReentrantLock bufferLock = new java.util.concurrent.locks.ReentrantLock();

	/**
	 * Constructs the service, initializes the WAL file, and attempts crash recovery.
	 *
	 * @param auditHandler         the handler responsible for persistent storage of audits
	 * @param auditRepository      repository reference (currently unused)
	 * @param objectMapper         JSON serializer/deserializer used for WAL lines
	 * @param bufferSize           maximum buffer capacity before forcing an immediate flush
	 * @param flushIntervalMillis  scheduled flush interval (ms)
	 * @param walFilePath          filesystem path for the WAL file; created if absent
	 */
	@Autowired
	public AuditManagerServiceImpl(
			AuditHandler<AuditRequestDto> auditHandler,
			AuditRepository auditRepository,
			ObjectMapper objectMapper,
			@Value("${mosip.kernel.auditmanager.buffer-size:1000}") int bufferSize,
			@Value("${mosip.kernel.auditmanager.flush-interval-millis:60000}") long flushIntervalMillis,
			@Value("${mosip.kernel.auditmanager.wal-file-path:./audit-wal.log}") String walFilePath,
			@Value("${mosip.kernel.auditmanager.retention-period-millis:2592000000}") long retentionPeriodMillis) {
		this.auditHandler = auditHandler;
		this.auditRepository = auditRepository;
		this.objectMapper = objectMapper;
		this.bufferSize = bufferSize;
		this.flushIntervalMillis = flushIntervalMillis;
		this.walFilePath = walFilePath;
		this.retentionPeriodMillis = retentionPeriodMillis;

		// Initialize WAL file and recover records
		this.walFile = new File(walFilePath);
		try {
			if (!walFile.exists()) {
				walFile.createNewFile();
			}
			recoverFromWalFile();
		} catch (IOException e) {
			logger.error("Failed to initialize WAL file: {}", e.getMessage());
		}
	}

	/**
	 * Replays the WAL file into the in-memory buffer on startup.
	 *
	 * <p>Each line is expected to contain a single JSON-serialized {@link AuditRequestDto}.
	 * Malformed lines are logged and skipped. Successfully parsed records are appended to
	 * {@link #auditBuffer} for eventual flush by {@link #flushAuditBuffer()}.</p>
	 */
	private void recoverFromWalFile() {
		try {
			List<String> lines = Files.readAllLines(walFile.toPath());
			List<AuditRequestDto> recoveredRecords = lines.stream()
					.map(line -> {
						try {
							return objectMapper.readValue(line, AuditRequestDto.class);
						} catch (Exception e) {
							logger.error("Failed to parse WAL record: {}", line, e);
							return null;
						}
					})
					.filter(record -> record != null)
					.collect(Collectors.toList());
			if (!recoveredRecords.isEmpty()) {
				logger.info("Recovered {} records from WAL file", recoveredRecords.size());
				auditBuffer.addAll(recoveredRecords);
			}
		} catch (IOException e) {
			logger.error("Failed to recover from WAL file: {}", e.getMessage());
		}
	}

	/**
	 * Appends a single audit request to the WAL file.
	 *
	 * <p>Best-effort: if the write fails, the exception is logged and the caller proceeds.</p>
	 *
	 * @param auditRequestDto the audit request to serialize and append
	 */
	private void appendToWalFile(AuditRequestDto auditRequestDto) {
		walLock.lock();
		try (FileWriter fw = new FileWriter(walFile, true);
			 BufferedWriter writer = new BufferedWriter(fw)) {
			writer.write(objectMapper.writeValueAsString(auditRequestDto));
			writer.newLine();
			writer.flush(); // ensure the line hits the OS buffers
		} catch (IOException e) {
			logger.error("Failed to append to WAL file: {}", e.getMessage());
		} finally {
			walLock.unlock();
		}
	}

	/**
	 * Appends a batch of audit requests to the WAL file.
	 *
	 * <p>Best-effort: if the write fails, the exception is logged and the caller proceeds.</p>
	 *
	 * @param auditRequestDtos the audit requests to serialize and append in sequence
	 */
	private void appendToWalFile(List<AuditRequestDto> auditRequestDtos) {
		walLock.lock();
		try (FileWriter fw = new FileWriter(walFile, true);
			 BufferedWriter writer = new BufferedWriter(fw)) {
			for (AuditRequestDto dto : auditRequestDtos) {
				writer.write(objectMapper.writeValueAsString(dto));
				writer.newLine();
			}
			writer.flush();
		} catch (IOException e) {
			logger.error("Failed to append batch to WAL file: {}", e.getMessage());
		} finally {
			walLock.unlock();
		}
	}

	/**
	 * Truncates the WAL file (clears its contents).
	 *
	 * <p>Invoked after a successful flush to the persistent store.</p>
	 */
	private void clearWalFile() {
		walLock.lock();
		try (FileWriter fw = new FileWriter(walFile, false)) { // truncate
			fw.flush();
		} catch (IOException e) {
			logger.error("Failed to clear WAL file: {}", e.getMessage());
		} finally {
			walLock.unlock();
		}
	}

	/**
	 * Adds a single audit record synchronously.
	 *
	 * <p>Bypasses the buffer and WAL; delegates directly to
	 * {@link AuditHandler#addAudit(Object)} for immediate persistence.</p>
	 *
	 * @param auditRequestDto audit payload to persist
	 * @return response indicating success/failure of the operation
	 */
	@Override
	public AuditResponseDto addAudit(AuditRequestDto auditRequestDto) {
		AuditResponseDto auditResponseDto = new AuditResponseDto();
		boolean success = auditHandler.addAudit(auditRequestDto);
		auditResponseDto.setStatus(success);
		return auditResponseDto;
	}

	/**
	 * Adds a single audit record asynchronously with durability.
	 *
	 * <p>Steps:
	 * <ol>
	 *   <li>If buffer capacity is reached, trigger an immediate {@link #flushAuditBuffer()}.</li>
	 *   <li>Append the record to the WAL file to survive process crashes.</li>
	 *   <li>Add the record to the in-memory buffer for batched flushing.</li>
	 * </ol>
	 *
	 * @param auditRequestDto audit payload to enqueue and persist later
	 */
	@Override
	@Async("auditExecutor")
	public void addAuditAsync(AuditRequestDto auditRequestDto) {
		if (auditBuffer.size() >= bufferSize) {
			logger.warn("Audit buffer reached capacity ({} records), triggering immediate flush", bufferSize);
			flushAuditBuffer();
		}
		appendToWalFile(auditRequestDto);
		auditBuffer.add(auditRequestDto);
	}

	/**
	 * Adds multiple audit records synchronously in a single operation.
	 *
	 * <p>Bypasses the buffer and WAL; delegates directly to
	 * {@link AuditHandler#addAudits(List)} for immediate batch persistence.</p>
	 *
	 * @param auditRequestDtos batch of audit payloads to persist
	 * @return response indicating success/failure of the batch operation
	 */
	@Override
	@Transactional
	public AuditResponseDto addAudits(List<AuditRequestDto> auditRequestDtos) {
		AuditResponseDto auditResponseDto = new AuditResponseDto();
		boolean success = auditHandler.addAudits(auditRequestDtos);
		auditResponseDto.setStatus(success);
		return auditResponseDto;
	}

	/**
	 * Adds multiple audit records asynchronously with durability.
	 *
	 * <p>Steps:
	 * <ol>
	 *   <li>If the combined size (buffer + batch) exceeds capacity, trigger a flush.</li>
	 *   <li>Append the entire batch to the WAL file.</li>
	 *   <li>Add all records to the in-memory buffer for later flush.</li>
	 * </ol>
	 *
	 * @param auditRequestDtos batch of audit payloads to enqueue and persist later
	 */
	@Override
	@Async("auditExecutor")
	public void addAuditsAsync(List<AuditRequestDto> auditRequestDtos) {
		if (auditBuffer.size() + auditRequestDtos.size() >= bufferSize) {
			logger.warn("Audit buffer nearing capacity ({} records), triggering immediate flush", bufferSize);
			flushAuditBuffer();
		}
		appendToWalFile(auditRequestDtos);
		auditBuffer.addAll(auditRequestDtos);
	}

	/**
	 * Updates multiple audit records synchronously in a single operation.
	 *
	 * <p>Delegates to {@link AuditHandler#updateAudits(List)}.</p>
	 *
	 * @param auditRequestDtos batch of audit payloads to update
	 * @return response indicating success/failure of the update batch
	 */
	@Override
	@Transactional
	public AuditResponseDto updateAudits(List<AuditRequestDto> auditRequestDtos) {
		AuditResponseDto auditResponseDto = new AuditResponseDto();
		boolean success = auditHandler.updateAudits(auditRequestDtos);
		auditResponseDto.setStatus(success);
		return auditResponseDto;
	}

	/**
	 * Updates multiple audit records asynchronously.
	 *
	 * <p>Behavior matches {@link #addAuditsAsync(List)} with WAL durability:
	 * batch is appended to WAL and buffered for scheduled flush.</p>
	 *
	 * @param auditRequestDtos batch of audit payloads to enqueue for update
	 */
	@Override
	@Async("auditExecutor")
	public void updateAuditsAsync(List<AuditRequestDto> auditRequestDtos) {
		if (auditBuffer.size() + auditRequestDtos.size() >= bufferSize) {
			logger.warn("Audit buffer nearing capacity ({} records), triggering immediate flush", bufferSize);
			flushAuditBuffer();
		}
		appendToWalFile(auditRequestDtos);
		auditBuffer.addAll(auditRequestDtos);
	}

	/**
	 * Flushes the in-memory buffer to the backing store.
	 *
	 * <p>Invoked automatically at a fixed rate defined by
	 * <code>mosip.kernel.auditmanager.flush-interval-millis</code> and also
	 * proactively when the buffer approaches capacity.</p>
	 *
	 * <p>On successful flush, the WAL is truncated. On failure, the buffered
	 * data remains in memory and the WAL is retained for a future retry.</p>
	 */
	@Scheduled(fixedRateString = "${mosip.kernel.auditmanager.flush-interval-millis:60000}")
	@Transactional
	public void flushAuditBuffer() {
		if (auditBuffer.isEmpty()) return;

		// Take a stable snapshot without losing concurrent appends
		final java.util.List<AuditRequestDto> batch;
		bufferLock.lock();
		try {
			if (auditBuffer.isEmpty()) return;
			batch = new java.util.ArrayList<>(auditBuffer); // snapshot
		} finally {
			bufferLock.unlock();
		}

		boolean success = false;
		long t0 = System.currentTimeMillis();
		try {
			success = auditHandler.addAudits(batch);
		} catch (Exception e) {
			logger.error("Failed to flush audit buffer", e);
		}

		// Only remove flushed items if success
		if (success) {
			bufferLock.lock();
			try {
				// remove exactly the flushed records; preserves any new arrivals
				auditBuffer.removeAll(batch);
			} finally {
				bufferLock.unlock();
			}
			clearWalFile();
		} else {
			// nothing removed; batch stays in buffer for retry
		}

		logger.info("Flushed {} audits in {} ms, success={}",
				batch.size(), System.currentTimeMillis() - t0, success);
	}

	/**
	 * Scheduled retention cleanup using configured {@code retentionPeriodMillis}.
	 */
	@Scheduled(cron = "${mosip.kernel.auditmanager.clear-cron:0 0 3 * * *}")
	@Transactional
	public void scheduledRetentionCleanup() {
		clearOldAudits(this.retentionPeriodMillis);
	}

	/**
	 * Deletes audit records older than the specified retention period.
	 *
	 * <p>Computes a cutoff timestamp of <code>now - retentionPeriodMillis</code>
	 * (rounded to seconds) and delegates to {@link AuditHandler#deleteAuditsOlderThan(LocalDateTime)}.</p>
	 *
	 * @param retentionPeriodMillis retention period in milliseconds; records strictly older than
	 *                              <em>now - retentionPeriodMillis</em> are eligible for deletion
	 */
	@Override
	@Transactional
	public void clearOldAudits(long retentionPeriodMillis) {
		LocalDateTime cutoffTime = LocalDateTime.now().minusSeconds(retentionPeriodMillis / 1000);
		boolean success = auditHandler.deleteAuditsOlderThan(cutoffTime);
		logger.info("Cleared old audits before {}, success: {}", cutoffTime, success);
	}
}