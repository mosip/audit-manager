package io.mosip.kernel.auditmanager.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.mosip.kernel.auditmanager.entity.Audit;
import io.mosip.kernel.auditmanager.repository.AuditRepository;
import io.mosip.kernel.auditmanager.request.AuditRequestDto;
import io.mosip.kernel.auditmanager.util.AuditUtils;
import io.mosip.kernel.core.auditmanager.spi.AuditHandler;

/**
 * Concrete {@link AuditHandler} implementation that persists audit events using
 * a Spring Data {@link AuditRepository}.
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Validate incoming {@link AuditRequestDto} (see {@link AuditUtils#validateAuditRequestDto(AuditRequestDto)}).</li>
 *   <li>Convert DTOs to JPA entities ({@link Audit}) for persistence.</li>
 *   <li>Perform single and batched create/update operations.</li>
 *   <li>Delete records older than a provided cutoff time.</li>
 * </ul>
 *
 * <h2>Transactions</h2>
 * <p>
 * All mutating operations are annotated with {@link Transactional}, ensuring that:
 * <ul>
 *   <li>Single audit writes are atomic.</li>
 *   <li>Batched writes/updates are committed or rolled back as a unit.</li>
 *   <li>Deletions older than a cutoff are performed within a single transaction boundary.</li>
 * </ul>
 * </p>
 *
 * <h2>Error Handling</h2>
 * <p>
 * Methods return a boolean to indicate success/failure and log the root cause.
 * <ul>
 *   <li>{@link DataAccessException} is caught explicitly for repository-related failures.</li>
 *   <li>{@link IllegalArgumentException} is used to signal validation errors.</li>
 *   <li>Any other {@link Exception} is caught, logged, and results in <code>false</code>.</li>
 * </ul>
 * </p>
 *
 * <h2>Performance Notes</h2>
 * <ul>
 *   <li>{@link #addAudits(List)} and {@link #updateAudits(List)} rely on repository batch methods
 *       ({@link AuditRepository#saveAll(Iterable)} and a custom {@link AuditRepository#batchUpdate(List)}),
 *       which significantly reduce round-trips and improve throughput for large volumes.</li>
 *   <li>DTO-to-entity conversion is performed via streams for readability; consider pre-sizing lists for
 *       very large batches if profiling shows pressure.</li>
 * </ul>
 * 
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 *
 */
@Service
public class AuditHandlerImpl implements AuditHandler<AuditRequestDto> {

	private static final Logger logger = LoggerFactory.getLogger(AuditHandlerImpl.class);

	/**
	 * Repository providing persistence operations for {@link Audit} entities.
	 */
	@Autowired
	private AuditRepository auditRepository;

	/**
	 * Persist a single audit event.
	 *
	 * <p>Flow:</p>
	 * <ol>
	 *   <li>Validate the request via {@link AuditUtils#validateAuditRequestDto(AuditRequestDto)}.</li>
	 *   <li>Convert it to an {@link Audit} entity.</li>
	 *   <li>Save using {@link AuditRepository#save(Object)}.</li>
	 * </ol>
	 *
	 * @param auditRequest the audit DTO to be persisted; must satisfy validation constraints
	 * @return {@code true} if persisted successfully; {@code false} if validation or persistence fails
	 *
	 * @implNote Validation errors throw {@link IllegalArgumentException} and are mapped to {@code false}.
	 */
	@Override
	@Transactional
	public boolean addAudit(AuditRequestDto auditRequest) {
		try {
			AuditUtils.validateAuditRequestDto(auditRequest);
			Audit event = convertToAuditEntity(auditRequest);
			auditRepository.save(event);
			logger.info("Successfully saved audit with eventId={}", auditRequest.getEventId());
			return true;
		} catch (IllegalArgumentException e) {
			logger.error("Invalid audit request for eventId={}: {}", auditRequest.getEventId(), e.getMessage());
			return false;
		} catch (DataAccessException dae) {
			logger.error("Database error while saving audit with eventId={}: {}", auditRequest.getEventId(), dae.getMessage());
			return false;
		} catch (Exception ex) {
			logger.error("Unexpected error in addAudit for eventId={}: {}", auditRequest.getEventId(), ex.getMessage());
			return false;
		}
	}

	/**
	 * Persist multiple audit events in a batch.
	 *
	 * <p>Converts each {@link AuditRequestDto} to {@link Audit} and calls {@link AuditRepository#saveAll(Iterable)}.
	 * Leveraging batch operations greatly improves throughput for large lists.</p>
	 *
	 * @param auditRequestDtos list of audit DTOs to persist; a {@code null} entry will cause an exception
	 * @return {@code true} if the batch persisted successfully; {@code false} otherwise
	 *
	 * @implNote If a single element is invalid or conversion fails, the entire call returns {@code false}.
	 */
	@Override
	@Transactional
	public boolean addAudits(List<AuditRequestDto> auditRequestDtos) {
		try {
			List<Audit> audits = auditRequestDtos.stream()
					.map(this::convertToAuditEntity)
					.collect(Collectors.toList());
			auditRepository.saveAll(audits); // Batch insert
			logger.info("Successfully saved {} audits in batch", audits.size());
			return true;
		} catch (Exception ex) {
			logger.error("Unexpected error in addAudits for batch size={}: {}", auditRequestDtos.size(), ex.getMessage());
			return false;
		}
	}

	/**
	 * Update multiple audit events in a batch.
	 *
	 * <p>Converts each {@link AuditRequestDto} to {@link Audit} and calls a repository-level
	 * {@link AuditRepository#batchUpdate(List)} for efficient mass updates (implementation-specific).</p>
	 *
	 * @param auditRequestDtos list of audit DTOs to update; must correspond to existing entities (by ID or unique keys)
	 * @return {@code true} if the batch update succeeds; {@code false} otherwise
	 *
	 * @implNote The semantics of "update" depend on the repository implementation (e.g., matching by primary key).
	 */
	@Override
	@Transactional
	public boolean updateAudits(List<AuditRequestDto> auditRequestDtos) {
		try {
			List<Audit> audits = auditRequestDtos.stream()
					.map(this::convertToAuditEntity)
					.collect(Collectors.toList());
			auditRepository.batchUpdate(audits); // Batch update
			logger.info("Successfully updated {} audits in batch", audits.size());
			return true;
		} catch (Exception ex) {
			logger.error("Unexpected error in updateAudits for batch size={}: {}", auditRequestDtos.size(), ex.getMessage());
			return false;
		}
	}

	/**
	 * Delete all audit records strictly older than the provided cutoff time.
	 *
	 * <p>Delegates to {@link AuditRepository#deleteAuditsOlderThan(LocalDateTime)}.</p>
	 *
	 * @param cutoffTime records with timestamp {@code < cutoffTime} will be removed
	 * @return {@code true} if at least one record was deleted; {@code false} otherwise
	 */
	@Override
	@Transactional
	public boolean deleteAuditsOlderThan(LocalDateTime cutoffTime) {
		try {
			int deletedCount = auditRepository.deleteAuditsOlderThan(cutoffTime);
			logger.info("Deleted {} audits older than {}", deletedCount, cutoffTime);
			return deletedCount > 0;
		} catch (Exception ex) {
			logger.error("Unexpected error in deleteAuditsOlderThan for cutoffTime={}: {}", cutoffTime, ex.getMessage());
			return false;
		}
	}

	/**
	 * Convert a validated {@link AuditRequestDto} into an {@link Audit} entity suitable for persistence.
	 *
	 * <p>The mapping is field-for-field, preserving identifiers, timestamps, module metadata,
	 * and user/session context.</p>
	 *
	 * @param auditRequestDto the source audit DTO (assumed validated)
	 * @return a new {@link Audit} entity populated from the DTO
	 */
	private Audit convertToAuditEntity(AuditRequestDto auditRequestDto) {
		Audit audit = new Audit();
		audit.setEventId(auditRequestDto.getEventId());
		audit.setEventName(auditRequestDto.getEventName());
		audit.setEventType(auditRequestDto.getEventType());
		audit.setActionTimeStamp(auditRequestDto.getActionTimeStamp());
		audit.setHostName(auditRequestDto.getHostName());
		audit.setHostIp(auditRequestDto.getHostIp());
		audit.setApplicationId(auditRequestDto.getApplicationId());
		audit.setApplicationName(auditRequestDto.getApplicationName());
		audit.setSessionUserId(auditRequestDto.getSessionUserId());
		audit.setSessionUserName(auditRequestDto.getSessionUserName());
		audit.setId(auditRequestDto.getId());
		audit.setIdType(auditRequestDto.getIdType());
		audit.setCreatedBy(auditRequestDto.getCreatedBy());
		audit.setModuleName(auditRequestDto.getModuleName());
		audit.setModuleId(auditRequestDto.getModuleId());
		audit.setDescription(auditRequestDto.getDescription());
		return audit;
	}

}
