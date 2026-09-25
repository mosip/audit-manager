package io.mosip.kernel.auditmanager.config;

import org.modelmapper.ModelMapper;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Audit API configuration: entity scan and ModelMapper bean.
 * Component scan is limited to API packages (not the service module).
 */
@Configuration
@EntityScan("io.mosip.kernel.auditmanager.entity")
@ComponentScan(basePackages = {
		"io.mosip.kernel.auditmanager.impl",
		"io.mosip.kernel.auditmanager.builder",
		"io.mosip.kernel.auditmanager.util",
		"io.mosip.kernel.auditmanager.repository"
})
public class AuditConfig {

	/**
	 * Creates a new ModelMapper bean.
	 *
	 * @return The {@link ModelMapper}
	 */
	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}

}
