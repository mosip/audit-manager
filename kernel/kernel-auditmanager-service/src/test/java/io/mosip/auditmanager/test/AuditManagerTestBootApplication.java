package io.mosip.auditmanager.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import io.mosip.kernel.auditmanager.test.config.TestSecurityConfig;

/**
 * Test bootstrap for Audit Manager — excludes unused kernel-core generators/validators.
 */
@SpringBootApplication
@Import(TestSecurityConfig.class)
@EntityScan("io.mosip.kernel.auditmanager.entity")
@EnableJpaRepositories("io.mosip.kernel.auditmanager.repository")
@EnableAutoConfiguration(excludeName = {
		"io.mosip.kernel.idgenerator.vid.impl.VidGeneratorImpl",
		"io.mosip.kernel.idgenerator.vid.util.VidFilterUtils",
		"io.mosip.kernel.idgenerator.tokenid.impl.TokenIdGeneratorImpl",
		"io.mosip.kernel.idgenerator.machineid.impl.MachineIdGeneratorImpl",
		"io.mosip.kernel.idgenerator.regcenterid.impl.RegistrationCenterIdGeneratorImpl",
		"io.mosip.kernel.idgenerator.mispid.impl.MispIdGeneratorImpl",
		"io.mosip.kernel.licensekeygenerator.misp.impl.MISPLicenseKeyGeneratorImpl",
		"io.mosip.kernel.licensekeygenerator.misp.util.MISPLicenseKeyGeneratorUtil",
		"io.mosip.kernel.idgenerator.rid.impl.RidGeneratorImpl",
		"io.mosip.kernel.idvalidator.prid.impl.PridValidatorImpl",
		"io.mosip.kernel.idvalidator.rid.impl.RidValidatorImpl",
		"io.mosip.kernel.idvalidator.uin.impl.UinValidatorImpl",
		"io.mosip.kernel.idvalidator.vid.impl.VidValidatorImpl",
		"io.mosip.kernel.idvalidator.mispid.impl.MispIdValidatorImpl",
		"io.mosip.kernel.templatemanager.velocity.builder.TemplateManagerBuilderImpl",
		"io.mosip.kernel.pdfgenerator.impl.PDFGeneratorImpl",
		"io.mosip.kernel.qrcode.generator.zxing.QrcodeGeneratorImpl",
		"io.mosip.kernel.transliteration.icu4j.impl.TransliterationImpl",
		"io.mosip.kernel.applicanttype.api.impl.ApplicantTypeImpl",
		"io.mosip.kernel.idobjectvalidator.config.IdObjectValidatorConfig",
		"io.mosip.kernel.websub.api.config.IntentVerificationConfig",
		"io.mosip.kernel.websub.api.config.WebSubClientConfig",
		"io.mosip.kernel.websub.api.config.publisher.WebSubPublisherClientConfig",
		"io.mosip.kernel.websub.api.config.publisher.RestTemplateHelper"
})
@ComponentScan(
		basePackages = { "io.mosip.kernel.auditmanager" },
		excludeFilters = @ComponentScan.Filter(
				type = FilterType.REGEX,
				pattern = "io\\.mosip\\.kernel\\.auth\\.defaultadapter\\.config\\.SecurityConfig"))
public class AuditManagerTestBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuditManagerTestBootApplication.class, args);
	}
}
