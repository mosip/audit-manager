package io.mosip.kernal.auditmanager.test;

import java.time.LocalDateTime;

import org.junit.Test;

import io.mosip.kernel.auditmanager.request.AuditRequestDto;
import io.mosip.kernel.auditmanager.util.AuditUtils;
import io.mosip.kernel.core.auditmanager.exception.AuditManagerException;

/**
 * Exhaustive validation coverage for {@link AuditUtils} (Sonar / JaCoCo 100%).
 */
public class AuditUtilsTest {

	private AuditRequestDto valid() {
		AuditRequestDto dto = new AuditRequestDto();
		dto.setActionTimeStamp(LocalDateTime.now());
		dto.setApplicationId("applicationId");
		dto.setApplicationName("applicationName");
		dto.setCreatedBy("createdBy");
		dto.setDescription("description");
		dto.setEventId("eventId");
		dto.setEventName("eventName");
		dto.setEventType("eventType");
		dto.setHostIp("hostIp");
		dto.setHostName("hostName");
		dto.setId("id");
		dto.setIdType("idType");
		dto.setModuleId("moduleId");
		dto.setModuleName("moduleName");
		dto.setSessionUserId("sessionUserId");
		dto.setSessionUserName("sessionUserName");
		return dto;
	}

	@Test
	public void validRequestPasses() {
		AuditUtils.validateAuditRequestDto(valid());
	}

	@Test(expected = AuditManagerException.class)
	public void nullRequest() {
		AuditUtils.validateAuditRequestDto(null);
	}

	@Test(expected = AuditManagerException.class)
	public void eventIdEmpty() {
		AuditRequestDto dto = valid();
		dto.setEventId("");
		AuditUtils.validateAuditRequestDto(dto);
	}

	@Test(expected = AuditManagerException.class)
	public void eventIdTooLong() {
		AuditRequestDto dto = valid();
		dto.setEventId("x".repeat(65));
		AuditUtils.validateAuditRequestDto(dto);
	}

	@Test(expected = AuditManagerException.class)
	public void eventNameNull() {
		AuditRequestDto dto = valid();
		dto.setEventName(null);
		AuditUtils.validateAuditRequestDto(dto);
	}

	@Test(expected = AuditManagerException.class)
	public void eventNameTooLong() {
		AuditRequestDto dto = valid();
		dto.setEventName("x".repeat(129));
		AuditUtils.validateAuditRequestDto(dto);
	}

	@Test(expected = AuditManagerException.class)
	public void eventTypeEmpty() {
		AuditRequestDto dto = valid();
		dto.setEventType("");
		AuditUtils.validateAuditRequestDto(dto);
	}

	@Test(expected = AuditManagerException.class)
	public void eventTypeTooLong() {
		AuditRequestDto dto = valid();
		dto.setEventType("x".repeat(65));
		AuditUtils.validateAuditRequestDto(dto);
	}

	@Test(expected = AuditManagerException.class)
	public void actionTimeStampNull() {
		AuditRequestDto dto = valid();
		dto.setActionTimeStamp(null);
		AuditUtils.validateAuditRequestDto(dto);
	}

	@Test(expected = AuditManagerException.class)
	public void hostNameEmpty() {
		AuditRequestDto dto = valid();
		dto.setHostName("");
		AuditUtils.validateAuditRequestDto(dto);
	}

	@Test(expected = AuditManagerException.class)
	public void hostNameTooLong() {
		AuditRequestDto dto = valid();
		dto.setHostName("x".repeat(129));
		AuditUtils.validateAuditRequestDto(dto);
	}

	@Test(expected = AuditManagerException.class)
	public void hostIpEmpty() {
		AuditRequestDto dto = valid();
		dto.setHostIp("");
		AuditUtils.validateAuditRequestDto(dto);
	}

	@Test(expected = AuditManagerException.class)
	public void hostIpTooLong() {
		AuditRequestDto dto = valid();
		dto.setHostIp("x".repeat(257));
		AuditUtils.validateAuditRequestDto(dto);
	}

	@Test(expected = AuditManagerException.class)
	public void applicationIdEmpty() {
		AuditRequestDto dto = valid();
		dto.setApplicationId("");
		AuditUtils.validateAuditRequestDto(dto);
	}

	@Test(expected = AuditManagerException.class)
	public void applicationIdTooLong() {
		AuditRequestDto dto = valid();
		dto.setApplicationId("x".repeat(65));
		AuditUtils.validateAuditRequestDto(dto);
	}

	@Test(expected = AuditManagerException.class)
	public void applicationNameEmpty() {
		AuditRequestDto dto = valid();
		dto.setApplicationName("");
		AuditUtils.validateAuditRequestDto(dto);
	}

	@Test(expected = AuditManagerException.class)
	public void applicationNameTooLong() {
		AuditRequestDto dto = valid();
		dto.setApplicationName("x".repeat(129));
		AuditUtils.validateAuditRequestDto(dto);
	}

	@Test(expected = AuditManagerException.class)
	public void sessionUserIdEmpty() {
		AuditRequestDto dto = valid();
		dto.setSessionUserId("");
		AuditUtils.validateAuditRequestDto(dto);
	}

	@Test(expected = AuditManagerException.class)
	public void sessionUserIdTooLong() {
		AuditRequestDto dto = valid();
		dto.setSessionUserId("x".repeat(257));
		AuditUtils.validateAuditRequestDto(dto);
	}

	@Test(expected = AuditManagerException.class)
	public void createdByEmpty() {
		AuditRequestDto dto = valid();
		dto.setCreatedBy("");
		AuditUtils.validateAuditRequestDto(dto);
	}

	@Test(expected = AuditManagerException.class)
	public void createdByTooLong() {
		AuditRequestDto dto = valid();
		dto.setCreatedBy("x".repeat(257));
		AuditUtils.validateAuditRequestDto(dto);
	}

	@Test(expected = AuditManagerException.class)
	public void sessionUserNameTooLong() {
		AuditRequestDto dto = valid();
		dto.setSessionUserName("x".repeat(129));
		AuditUtils.validateAuditRequestDto(dto);
	}

	@Test
	public void sessionUserNameNullOk() {
		AuditRequestDto dto = valid();
		dto.setSessionUserName(null);
		AuditUtils.validateAuditRequestDto(dto);
	}

	@Test(expected = AuditManagerException.class)
	public void idTooLong() {
		AuditRequestDto dto = valid();
		dto.setId("x".repeat(65));
		AuditUtils.validateAuditRequestDto(dto);
	}

	@Test
	public void idNullOk() {
		AuditRequestDto dto = valid();
		dto.setId(null);
		AuditUtils.validateAuditRequestDto(dto);
	}

	@Test(expected = AuditManagerException.class)
	public void idTypeTooLong() {
		AuditRequestDto dto = valid();
		dto.setIdType("x".repeat(65));
		AuditUtils.validateAuditRequestDto(dto);
	}

	@Test
	public void idTypeNullOk() {
		AuditRequestDto dto = valid();
		dto.setIdType(null);
		AuditUtils.validateAuditRequestDto(dto);
	}

	@Test(expected = AuditManagerException.class)
	public void moduleNameTooLong() {
		AuditRequestDto dto = valid();
		dto.setModuleName("x".repeat(129));
		AuditUtils.validateAuditRequestDto(dto);
	}

	@Test
	public void moduleNameNullOk() {
		AuditRequestDto dto = valid();
		dto.setModuleName(null);
		AuditUtils.validateAuditRequestDto(dto);
	}

	@Test(expected = AuditManagerException.class)
	public void moduleIdTooLong() {
		AuditRequestDto dto = valid();
		dto.setModuleId("x".repeat(65));
		AuditUtils.validateAuditRequestDto(dto);
	}

	@Test
	public void moduleIdNullOk() {
		AuditRequestDto dto = valid();
		dto.setModuleId(null);
		AuditUtils.validateAuditRequestDto(dto);
	}

	@Test(expected = AuditManagerException.class)
	public void descriptionTooLong() {
		AuditRequestDto dto = valid();
		dto.setDescription("x".repeat(2049));
		AuditUtils.validateAuditRequestDto(dto);
	}

	@Test
	public void descriptionNullOk() {
		AuditRequestDto dto = valid();
		dto.setDescription(null);
		AuditUtils.validateAuditRequestDto(dto);
	}
}
