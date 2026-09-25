package io.mosip.kernel.auditmanager.config;

import java.util.List;
import java.util.stream.Collectors;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

/**
 * Springdoc OpenAPI / Swagger UI configuration.
 * Authorize button uses header apiKey {@code Authorization} (same as
 * kernel-auth-service / kernel-notification-service / bio-converter).
 */
@Configuration
public class SwaggerConfig {

	/**
	 * Scheme name shown by Swagger UI as Authorize.
	 */
	public static final String AUTHORIZATION_SCHEME = "Authorization";

	@Autowired
	private OpenApiProperties openApiProperties;

	@Bean
	public OpenAPI openApi() {
		List<Server> servers = openApiProperties.getService().getServers().stream()
				.map(server -> new Server().description(server.getDescription()).url(server.getUrl()))
				.collect(Collectors.toList());

		return new OpenAPI()
				.components(new Components().addSecuritySchemes(AUTHORIZATION_SCHEME, authorizationApiKey()))
				.addSecurityItem(new SecurityRequirement().addList(AUTHORIZATION_SCHEME))
				.info(new Info().title(openApiProperties.getInfo().getTitle())
						.version(openApiProperties.getInfo().getVersion())
						.description(openApiProperties.getInfo().getDescription())
						.license(new License().name(openApiProperties.getInfo().getLicense().getName())
								.url(openApiProperties.getInfo().getLicense().getUrl())))
				.servers(servers);
	}

	/**
	 * Header apiKey named {@code Authorization} so Swagger UI shows Authorize
	 * (paste {@code Bearer <token>} or the raw token as used by the gateway/adapter).
	 */
	private static SecurityScheme authorizationApiKey() {
		return new SecurityScheme().type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.HEADER)
				.name(AUTHORIZATION_SCHEME);
	}

	@Bean
	public GroupedOpenApi groupedOpenApi() {
		return GroupedOpenApi.builder().group(openApiProperties.getGroup().getName())
				.pathsToMatch(openApiProperties.getGroup().getPaths().toArray(String[]::new)).build();
	}
}
