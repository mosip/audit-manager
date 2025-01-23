package io.mosip.kernel.auditmanager.util;

import io.mosip.kernel.auditmanager.request.AuditRequestDto;

/**
 * Utility class for Audit Manager
 *
 * @author Dhanendra
 * @since 1.0.0
 *
 */
public class AuditUtils {

	/**
	 * Private constructor to prevent instantiation
	 */
	private AuditUtils() {

	}

	/**
	 * Function to validate {@link AuditRequestDto}
	 *
	 * @param auditRequestDto The audit request object to validate.
	 * @throws IllegalArgumentException if validation fails.
	 */
	public static void validateAuditRequestDto(AuditRequestDto auditRequestDto) {
		if (auditRequestDto == null) {
			throw new IllegalArgumentException("Audit request cannot be null");
		}

		StringBuilder errorMessages = new StringBuilder();

		// Validate each field manually
		if (auditRequestDto.getEventId() == null || auditRequestDto.getEventId().length() < 1 || auditRequestDto.getEventId().length() > 64) {
			errorMessages.append("eventId must be between 1 and 64 characters.\n");
		}
		if (auditRequestDto.getEventName() == null || auditRequestDto.getEventName().length() < 1 || auditRequestDto.getEventName().length() > 128) {
			errorMessages.append("eventName must be between 1 and 128 characters.\n");
		}
		if (auditRequestDto.getEventType() == null || auditRequestDto.getEventType().length() < 1 || auditRequestDto.getEventType().length() > 64) {
			errorMessages.append("eventType must be between 1 and 64 characters.\n");
		}
		if (auditRequestDto.getActionTimeStamp() == null) {
			errorMessages.append("actionTimeStamp must not be null.\n");
		}
		if (auditRequestDto.getHostName() == null || auditRequestDto.getHostName().length() < 1 || auditRequestDto.getHostName().length() > 128) {
			errorMessages.append("hostName must be between 1 and 128 characters.\n");
		}
		if (auditRequestDto.getHostIp() == null || auditRequestDto.getHostIp().length() < 1 || auditRequestDto.getHostIp().length() > 256) {
			errorMessages.append("hostIp must be between 1 and 256 characters.\n");
		}
		if (auditRequestDto.getApplicationId() == null || auditRequestDto.getApplicationId().length() < 1 || auditRequestDto.getApplicationId().length() > 64) {
			errorMessages.append("applicationId must be between 1 and 64 characters.\n");
		}
		if (auditRequestDto.getApplicationName() == null || auditRequestDto.getApplicationName().length() < 1 || auditRequestDto.getApplicationName().length() > 128) {
			errorMessages.append("applicationName must be between 1 and 128 characters.\n");
		}
		if (auditRequestDto.getSessionUserId() == null || auditRequestDto.getSessionUserId().length() < 1 || auditRequestDto.getSessionUserId().length() > 256) {
			errorMessages.append("sessionUserId must be between 1 and 256 characters.\n");
		}
		if (auditRequestDto.getCreatedBy() == null || auditRequestDto.getCreatedBy().length() < 1 || auditRequestDto.getCreatedBy().length() > 256) {
			errorMessages.append("createdBy must be between 1 and 256 characters.\n");
		}
		if (auditRequestDto.getSessionUserName() != null && auditRequestDto.getSessionUserName().length() > 128) {
			errorMessages.append("sessionUserName must not exceed 128 characters.\n");
		}
		if (auditRequestDto.getId() != null && auditRequestDto.getId().length() > 64) {
			errorMessages.append("id must not exceed 64 characters.\n");
		}
		if (auditRequestDto.getIdType() != null && auditRequestDto.getIdType().length() > 64) {
			errorMessages.append("idType must not exceed 64 characters.\n");
		}
		if (auditRequestDto.getModuleName() != null && auditRequestDto.getModuleName().length() > 128) {
			errorMessages.append("moduleName must not exceed 128 characters.\n");
		}
		if (auditRequestDto.getModuleId() != null && auditRequestDto.getModuleId().length() > 64) {
			errorMessages.append("moduleId must not exceed 64 characters.\n");
		}
		if (auditRequestDto.getDescription() != null && auditRequestDto.getDescription().length() > 2048) {
			errorMessages.append("description must not exceed 2048 characters.\n");
		}

		// Throw exception if there are validation errors
		if (errorMessages.length() > 0) {
			throw new IllegalArgumentException(errorMessages.toString().trim());
		}
	}
}
