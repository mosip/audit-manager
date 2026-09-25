package io.mosip.kernal.auditmanager.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.time.LocalDateTime;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;

import io.mosip.kernel.auditmanager.builder.AuditRequestBuilder;
import io.mosip.kernel.auditmanager.config.AuditConfig;
import io.mosip.kernel.auditmanager.entity.Audit;
import io.mosip.kernel.auditmanager.impl.AuditHandlerImpl;
import io.mosip.kernel.auditmanager.repository.AuditRepository;
import io.mosip.kernel.auditmanager.request.AuditRequestDto;
import io.mosip.kernel.auditmanager.util.AuditUtils;
import io.mosip.kernel.core.auditmanager.exception.AuditManagerException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AuditConfig.class)
public class AuditEventTest {

	@Autowired
	private AuditHandlerImpl auditHandlerImpl;

	@MockitoBean
	private AuditRepository auditRepository;

	private AuditRequestDto validRequest() {
		return new AuditRequestBuilder().setActionTimeStamp(LocalDateTime.now()).setApplicationId("applicationId")
				.setApplicationName("applicationName").setCreatedBy("createdBy").setDescription("description")
				.setEventId("eventId").setEventName("eventName").setEventType("eventType").setHostIp("hostIp")
				.setHostName("hostName").setId("id").setIdType("idType").setModuleId("moduleId")
				.setModuleName("moduleName").setSessionUserId("sessionUserId").setSessionUserName("sessionUserName")
				.build();
	}

	@Test
	public void auditBuilderTest() {
		Mockito.when(auditRepository.save(ArgumentMatchers.any(Audit.class))).thenReturn(new Audit());
		assertThat(auditHandlerImpl.addAudit(validRequest()), is(true));
	}

	@Test
	public void auditDataAccessFailureReturnsFalse() {
		Mockito.when(auditRepository.save(ArgumentMatchers.any(Audit.class)))
				.thenThrow(new DataAccessResourceFailureException("db down"));
		assertThat(auditHandlerImpl.addAudit(validRequest()), is(false));
	}

	@Test(expected = AuditManagerException.class)
	public void auditBuilderExceptionTest() {
		AuditRequestBuilder auditRequestBuilder = new AuditRequestBuilder();
		auditRequestBuilder.setApplicationId("applicationId").setApplicationName("applicationName")
				.setCreatedBy("createdBy").setDescription("description").setEventId("eventId").setEventName("eventName")
				.setEventType("eventType").setHostIp("hostIp").setHostName("hostName").setId("id").setIdType("idType")
				.setModuleId("moduleId").setModuleName("moduleName").setSessionUserId("sessionUserId")
				.setSessionUserName("sessionUserName");
		auditHandlerImpl.addAudit(auditRequestBuilder.build());
	}

	@Test(expected = AuditManagerException.class)
	public void validateNullRequest() {
		AuditUtils.validateAuditRequestDto(null);
	}

	@Test(expected = AuditManagerException.class)
	public void validateOptionalFieldTooLong() {
		AuditRequestDto dto = validRequest();
		dto.setDescription("x".repeat(2049));
		AuditUtils.validateAuditRequestDto(dto);
	}

}