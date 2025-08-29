/**
 * 
 */
package io.mosip.kernel.auditmanager.service;

import io.mosip.kernel.auditmanager.dto.AuditResponseDto;
import io.mosip.kernel.auditmanager.entity.Audit;
import io.mosip.kernel.auditmanager.request.AuditRequestDto;

import java.util.List;

/**
 * Interface for AuditManager Serivce having function to add new {@link Audit}
 *
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 *
 */
public interface AuditManagerService {

	/**
	 * Function to add a single audit
	 *
	 * @param auditRequestDto The {@link AuditRequestDto} having required fields to audit
	 * @return The {@link AuditResponseDto} having status of audit
	 */
	AuditResponseDto addAudit(AuditRequestDto auditRequestDto);

	/**
	 * Function to asynchronously add a single audit
	 *
	 * @param auditRequestDto The {@link AuditRequestDto} to add
	 */
	void addAuditAsync(AuditRequestDto auditRequestDto);

	/**
	 * Function to add multiple audits in a batch
	 *
	 * @param auditRequestDtos The list of {@link AuditRequestDto} to add
	 * @return The {@link AuditResponseDto} having status of audit
	 */
	AuditResponseDto addAudits(List<AuditRequestDto> auditRequestDtos);

	/**
	 * Function to asynchronously add multiple audits in a batch
	 *
	 * @param auditRequestDtos The list of {@link AuditRequestDto} to add
	 */
	void addAuditsAsync(List<AuditRequestDto> auditRequestDtos);

	/**
	 * Function to update multiple audits in a batch
	 *
	 * @param auditRequestDtos The list of {@link AuditRequestDto} to update
	 * @return The {@link AuditResponseDto} having status of audit
	 */
	AuditResponseDto updateAudits(List<AuditRequestDto> auditRequestDtos);

	/**
	 * Function to asynchronously update multiple audits in a batch
	 *
	 * @param auditRequestDtos The list of {@link AuditRequestDto} to update
	 */
	void updateAuditsAsync(List<AuditRequestDto> auditRequestDtos);

	/**
	 * Function to delete audits older than the specified retention period
	 *
	 * @param retentionPeriodMillis The retention period in milliseconds
	 */
	void clearOldAudits(long retentionPeriodMillis);
}