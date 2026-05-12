package io.mosip.kernel.auditmanager.impl;

import io.mosip.kernel.core.logger.spi.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import io.mosip.kernel.auditmanager.entity.Audit;
import io.mosip.kernel.auditmanager.repository.AuditRepository;
import io.mosip.kernel.auditmanager.request.AuditRequestDto;
import io.mosip.kernel.auditmanager.util.AuditUtils;
import io.mosip.kernel.core.auditmanager.spi.AuditHandler;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link AuditHandler} with function to write
 * {@link AuditRequestDto}
 * 
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 *
 */
@Service
public class AuditHandlerImpl implements AuditHandler<AuditRequestDto> {

	/**
	 * Field for {@link AuditRepository} having data access operations related to
	 * audit
	 */
	@Autowired
	private AuditRepository auditRepository;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.core.audit.handler.AuditHandler#writeAudit(io.mosip.kernel.
	 * core.audit.dto.AuditRequest)
	 */
	@Override
	@Transactional
	public boolean addAudit(AuditRequestDto auditRequest) {
		try {
			AuditUtils.validateAuditRequestDto(auditRequest);
			Audit event = getAuditEntity(auditRequest);
			auditRepository.save(event);
			return true;
		} catch (DataAccessException ex) {
			return false;
		}
	}


	public Audit getAuditEntity(AuditRequestDto auditRequestDto) {
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
