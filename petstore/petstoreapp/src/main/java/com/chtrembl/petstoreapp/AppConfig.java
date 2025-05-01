package com.chtrembl.petstoreapp;

import com.azure.spring.cloud.autoconfigure.aadb2c.AadB2cAuthorizationRequestResolver;
import com.azure.spring.cloud.autoconfigure.aadb2c.AadB2cLogoutSuccessHandler;
import com.azure.spring.cloud.autoconfigure.aadb2c.AadB2cOidcLoginConfigurer;
import com.azure.spring.cloud.autoconfigure.aadb2c.properties.AadB2cProperties;
import com.azure.spring.cloud.core.properties.AzureProperties;
import com.chtrembl.petstoreapp.security.AADB2COidcLoginConfigurerWrapper;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.jsonwebtoken.lang.Assert;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Configuration
@EnableAutoConfiguration
@EnableCaching
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
@ComponentScan
public class AppConfig implements WebMvcConfigurer {


	@Override
	public void addInterceptors(InterceptorRegistry registry) {
	}

	@Bean
	public ClientRegistrationRepository clientRegistrationRepository(AadB2cProperties azureB2CProperties) {
		// Fetch redirect URL dynamically from application.yml based on profile
		String redirectUri = azureB2CProperties.getReplyUrl();

		ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("azure-b2c")
				.clientId(azureB2CProperties.getCredential().getClientId())
				.clientSecret(azureB2CProperties.getCredential().getClientSecret())
				.authorizationUri("https://module10epamtenant.b2clogin.com/module10epamtenant.onmicrosoft.com/oauth2/v2.0/authorize?code_challenge=2ZSi2IWQ35D_SCpWtO5yyyCaLUVb3NJjAhezhsRH96s&code_challenge_method=S256&p=b2c_1_signup_and_signin_v2")
				.tokenUri(azureB2CProperties.getBaseUri() + "/oauth2/v2.0/token?p=" + azureB2CProperties.getLoginFlow())
				.redirectUri(redirectUri) // Use environment appropriate redirect URL
				.scope("openid", "email", "profile")
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.build();

		return new InMemoryClientRegistrationRepository(clientRegistration);
	}

	@Bean
	public OAuth2AuthorizationRequestResolver getOAuth2AuthorizationRequestResolver(AadB2cProperties properties) {
		return new OAuth2AuthorizationRequestResolver() {

			@Override
			public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
				// Extract the path or endpoint to decide on the user flow
				String requestUri = request.getRequestURI();

				if (requestUri.contains("/oauth2/authorization")) {
					// Build the OAuth2 Authorization Request for the "sign-up-or-sign-in" flow
					String flowName = properties.getLoginFlow(); // Default login flow
					return createAuthorizationRequest(flowName, properties);
				}

				return null;
			}

			@Override
			public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
				Assert.notNull(clientRegistrationId, "Client registration ID must not be null");

				// Resolve a specific user flow or client registration
				if (clientRegistrationId.equals("sign-up-or-sign-in")) {
					return createAuthorizationRequest(properties.getUserFlows().get("sign-up-or-sign-in"), properties);
				}

				return null;
			}

			private OAuth2AuthorizationRequest createAuthorizationRequest(String flowName, AadB2cProperties properties) {

				return OAuth2AuthorizationRequest
						.authorizationCode()
						.clientId(properties.getCredential().getClientId())
						.authorizationUri("https://module10epamtenant.b2clogin.com/module10epamtenant.onmicrosoft.com/B2C_1_signup_and_signin_v2/oauth2/v2.0/authorize")
						.redirectUri(StringEscapeUtils.escapeHtml4("http://localhost:8080"))
						.scope("openid")
						.additionalParameters(new HashMap<>() {{
							this.put("p", "b2c_1_signup_and_signin_v2");
							this.put("nonce","defaultNonce");
							this.put("response_type","id_token");
							this.put("prompt","login");
						}})
						.state("default-state")
						.build();
			}
		};
	}

	@Bean
	@ConfigurationProperties(prefix = "spring.cloud.azure.active-directory.b2c") // Loads properties under this prefix
	public AadB2cProperties azureB2cProperties() {
		return new AadB2cProperties();
	}

	@Bean
	public OAuth2AccessTokenResponseClient getOAuth2AccessTokenResponseClient() {
		return new OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest>() {

			@Override
			public OAuth2AccessTokenResponse getTokenResponse(OAuth2AuthorizationCodeGrantRequest authorizationCodeGrantRequest) {
				// Custom logic for interacting with the token endpoint
				// You can log, validate, or modify the default behavior here

				// Use the default client for standard implementation
				DefaultAuthorizationCodeTokenResponseClient defaultClient = new DefaultAuthorizationCodeTokenResponseClient();
				return defaultClient.getTokenResponse(authorizationCodeGrantRequest);
			}
		};
	}

	@Bean
	public AadB2cAuthorizationRequestResolver getAadB2cAuthorizationRequestResolver(AadB2cProperties properties, OAuth2AuthorizationRequestResolver resolver) {
		return new AadB2cAuthorizationRequestResolver(properties, resolver);
	}

	@Bean
	public AadB2cLogoutSuccessHandler getAadB2cLogoutSuccessHandler(AadB2cProperties properties) {
		return new AadB2cLogoutSuccessHandler(properties);
	}

	@Bean
	public AadB2cOidcLoginConfigurer getAadB2cOidcLoginConfigurer(AadB2cLogoutSuccessHandler handler,
																  AadB2cAuthorizationRequestResolver resolver,
																  OAuth2AccessTokenResponseClient oAuth2AccessTokenResponseClient) {
		return new AadB2cOidcLoginConfigurer(handler, resolver, oAuth2AccessTokenResponseClient, new RestTemplateBuilder());
	}

	@Bean
	public AADB2COidcLoginConfigurerWrapper getAADB2COidcLoginConfigurerWrapper(AadB2cOidcLoginConfigurer aadB2cOidcLoginConfigurer) {
		AADB2COidcLoginConfigurerWrapper aadb2COidcLoginConfigurerWrapper = new AADB2COidcLoginConfigurerWrapper();

		aadb2COidcLoginConfigurerWrapper.setConfigurer(aadB2cOidcLoginConfigurer);

		return 	aadb2COidcLoginConfigurerWrapper;
	}

	@Bean
	public Caffeine caffeineConfig() {
		return Caffeine.newBuilder().expireAfterAccess(300, TimeUnit.SECONDS);
	}

	@Bean
	public CacheManager currentUsersCacheManager(Caffeine caffeine) {
		CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
		caffeineCacheManager.setCaffeine(caffeine);
		return caffeineCacheManager;
	}


}