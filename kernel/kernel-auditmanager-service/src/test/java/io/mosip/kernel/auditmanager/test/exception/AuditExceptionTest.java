package io.mosip.kernel.auditmanager.test.exception;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import io.mosip.auditmanager.test.AuditManagerTestBootApplication;
import io.mosip.kernel.auditmanager.repository.AuditRepository;
import io.mosip.kernel.auditmanager.service.impl.AuditManagerServiceImpl;

@SpringBootTest(classes = { AuditManagerTestBootApplication.class })
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class AuditExceptionTest {
	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AuditManagerServiceImpl service;

	@MockitoBean
	private AuditRepository auditRepository;

	@WithUserDetails("reg-processor")
	@Test
	public void auditInvalidRequestExceptionTest() throws Exception {
		String json = "{\r\n" + "  \"eventName\": \"string\",\r\n" + "  \"eventType\": \"string\",\r\n"
				+ "  \"actionTimeStamp\": \"2018-09-10T11:39:28.191Z\",\r\n" + "  \"hostName\": \"string\",\r\n"
				+ "  \"hostIp\": \"string\",\r\n" + "  \"applicationId\": \"string\",\r\n"
				+ "  \"applicationName\": \"string\",\r\n" + "  \"sessionUserId\": \"string\",\r\n"
				+ "  \"sessionUserName\": \"string\",\r\n" + "  \"id\": \"string\",\r\n"
				+ "  \"idType\": \"string\",\r\n" + "  \"createdBy\": \"string\",\r\n"
				+ "  \"moduleName\": \"string\",\r\n" + "  \"moduleId\": \"string\",\r\n"
				+ "  \"description\": \"string\"\r\n" + "}";
		mockMvc.perform(post("/audits").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk()).andExpect(jsonPath("$.errors[0].errorCode", is("KER-AUD-001")));
	}

	@WithUserDetails("reg-processor")
	@Test
	public void auditConstraintExceptionTest() throws Exception {

		String json = "{\r\n" + "  \"eventId\": \"\",\r\n" + "  \"eventName\": \"string\",\r\n"
				+ "  \"eventType\": \"string\",\r\n" + "  \"actionTimeStamp\": \"2018-09-10T11:39:28.191Z\",\r\n"
				+ "  \"hostName\": \"string\",\r\n" + "  \"hostIp\": \"string\",\r\n"
				+ "  \"applicationId\": \"string\",\r\n" + "  \"applicationName\": \"string\",\r\n"
				+ "  \"sessionUserId\": \"string\",\r\n" + "  \"sessionUserName\": \"string\",\r\n"
				+ "  \"id\": \"string\",\r\n" + "  \"idType\": \"string\",\r\n" + "  \"createdBy\": \"string\",\r\n"
				+ "  \"moduleName\": \"string\",\r\n" + "  \"moduleId\": \"string\",\r\n"
				+ "  \"description\": \"string\"\r\n" + "}";
		mockMvc.perform(post("/audits").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk()).andExpect(jsonPath("$.errors[0].errorCode", is("KER-AUD-001")));
	}

}
