package com.chtrembl.petstoreapp;

import com.azure.spring.cloud.autoconfigure.aadb2c.AadB2cAuthorizationRequestResolver;
import com.azure.spring.cloud.autoconfigure.aadb2c.AadB2cLogoutSuccessHandler;
import com.azure.spring.cloud.autoconfigure.aadb2c.AadB2cOidcLoginConfigurer;
import com.azure.spring.cloud.autoconfigure.aadb2c.properties.AadB2cProperties;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import io.jsonwebtoken.lang.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.log.LogMessage;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationProvider;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequestEntityConverter;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableAutoConfiguration
@EnableCaching
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
@EnableWebSecurity
@ComponentScan(basePackages = "com.chtrembl.petstoreapp.security")
public class AppConfig implements WebMvcConfigurer {

	private static Logger logger = LoggerFactory.getLogger(AppConfig.class);

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
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

	@Bean
	public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> customAccessTokenResponseClient() {
		RestTemplate restTemplate = new RestTemplate(); // To execute the HTTP request manually

		return new OAuth2AccessTokenResponseClient<>() {
			@Override
			public OAuth2AccessTokenResponse getTokenResponse(OAuth2AuthorizationCodeGrantRequest authorizationCodeGrantRequest) {
				// Retrieve registrationId (client identifier)
				String registrationId = authorizationCodeGrantRequest.getClientRegistration().getRegistrationId();

				// Retrieve the stored code_verifier for the current authorization flow
				String codeVerifier = PkceUtil.getCodeVerifier(registrationId);
				if (codeVerifier == null || codeVerifier.isEmpty()) {
					throw new RuntimeException("Code verifier missing for registrationId: " + registrationId);
				}

				// Retrieve the original token request entity
				OAuth2AuthorizationCodeGrantRequestEntityConverter requestConverter = new OAuth2AuthorizationCodeGrantRequestEntityConverter();
				RequestEntity<?> originalRequestEntity = requestConverter.convert(authorizationCodeGrantRequest);

				// Add the code_verifier to the HTTP request body
				HttpHeaders headers = new HttpHeaders(originalRequestEntity.getHeaders());
				MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
				body.addAll((MultiValueMap<String, String>) originalRequestEntity.getBody());
				body.add("code_verifier", codeVerifier); // ***** Add PKCE code_verifier here

				// Create a modified RequestEntity object
				RequestEntity<?> modifiedRequestEntity = new RequestEntity<>(
						body,
						headers,
						originalRequestEntity.getMethod(),
						originalRequestEntity.getUrl()
				);

				// Execute the HTTP request manually using RestTemplate
				ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(
						modifiedRequestEntity,
						new ParameterizedTypeReference<>() {}
				);

				// Parse the response body into an OAuth2AccessTokenResponse
				Map<String, Object> responseBody = responseEntity.getBody();
				if (responseBody == null) {
					throw new RuntimeException("Empty response body from the token endpoint.");
				}

				// Extract the id_token from the response
				String idToken = (String) responseBody.get("id_token");
				if (idToken == null) {
					throw new RuntimeException("id_token missing from token response.");
				}

				return parseIdTokenResponse(idToken, responseBody);
			}

			private OAuth2AccessTokenResponse parseIdTokenResponse(String idToken, Map<String, Object> responseBody) {
				// Extract additional fields from the response (e.g., refresh_token, custom claims)
				String refreshToken = (String) responseBody.get("refresh_token");
				long refreshTokenExpiresIn = responseBody.containsKey("refresh_token_expires_in")
						? ((Number) responseBody.get("refresh_token_expires_in")).longValue()
						: 0;

				// Scopes (if available)
				Set<String> scopes = Collections.emptySet();
				if (responseBody.containsKey("scope")) {
					String scope = (String) responseBody.get("scope");
					scopes = new HashSet<>(Arrays.asList(scope.split(" ")));
				}

				try {

					// Parse and extract claims from id_token
					JWTClaimsSet claims = parseIdTokenClaims(idToken);

					// Extract useful claims
					String issuer = claims.getIssuer(); // Azure AD B2C issuer
					String audience = claims.getAudience().get(0); // Your app's client ID
					String claimName = claims.getStringClaim("name");
					String claimEmail = claims.getStringArrayClaim("emails")[0]; // Use first email

					logger.info("Issuer: " + issuer);
					logger.info("Audience: " + audience);
					logger.info("Name: " + claimName);
					logger.info("Email: " + claimEmail);

					// Build the OAuth2AccessTokenResponse with additional parameters
					return OAuth2AccessTokenResponse.withToken(idToken)
							.tokenType(OAuth2AccessToken.TokenType.BEARER)
							.scopes(scopes)
							.refreshToken(refreshToken)
							.additionalParameters(Map.of(
									"refresh_token_expires_in", refreshTokenExpiresIn,
									"id_token", idToken,
									"name", claimName,
									"email", claimEmail
							))
							.build();

				} catch (IllegalArgumentException | IllegalStateException | ParseException e) {
					System.err.println("Error processing response: " + e.getMessage());
					throw new RuntimeException("Failed to process token response", e);
				}
			}
		};
	}

	@Bean
	public OAuth2LoginAuthenticationFilter getOAuth2LoginAuthenticationFilter(
			ClientRegistrationRepository clientRegistrationRepository,
			OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient,
			OAuth2AuthorizedClientService oAuth2AuthorizedClientService) {

		AuthenticationManager authenticationManager = new ProviderManager(
				List.of(new OAuth2LoginAuthenticationProvider(accessTokenResponseClient, new DefaultOAuth2UserService()))
		);

		OAuth2LoginAuthenticationFilter oAuth2LoginAuthenticationFilter =
				new OAuth2LoginAuthenticationFilter(clientRegistrationRepository, oAuth2AuthorizedClientService);

		oAuth2LoginAuthenticationFilter.setAuthenticationManager(authenticationManager);

		return oAuth2LoginAuthenticationFilter;
	}

	@Bean
	public OAuth2AuthorizedClientService getOAuth2AuthorizedClientService() {
		return  new OAuth2AuthorizedClientService() {
			@Override
			public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(String clientRegistrationId, String principalName) {
				return null;
			}

			@Override
			public void saveAuthorizedClient(OAuth2AuthorizedClient authorizedClient, Authentication principal) {
				// to add
			}

			@Override
			public void removeAuthorizedClient(String clientRegistrationId, String principalName) {
			}
		};
	}

	@Bean
	public ClientRegistrationRepository clientRegistrationRepository(AadB2cProperties azureB2CProperties) {

		String baseRequest = getBaseRequest(azureB2CProperties);

		ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("petstore-app")
				.clientId(azureB2CProperties.getCredential().getClientId())
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.redirectUri(azureB2CProperties.getReplyUrl())
				.scope("openid")
				.authorizationUri(baseRequest + "/oauth2/v2.0/authorize")
				.tokenUri(baseRequest+ "/oauth2/v2.0/token")
				.userInfoUri(baseRequest+ "/openid/v1.0/")
				.jwkSetUri(baseRequest+ "/discovery/v2.0/keys")
				.clientName("PetStore App")
				.build();

		return new TmpInMemoryClientRegistrationRepository(clientRegistration);
	}

	private static String getBaseRequest(AadB2cProperties azureB2CProperties) {
		return String.format("%s%s.onmicrosoft.com/%s"
				, azureB2CProperties.getBaseUri()
				, azureB2CProperties.getProfile().getTenantId()
				, azureB2CProperties.getUserFlows().get("sign-up-or-sign-in")
		);
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

			private OAuth2AuthorizationRequest createAuthorizationRequest(String flowName, AadB2cProperties azureB2CProperties) {

				String authorizationUri = getBaseRequest(azureB2CProperties) + "/oauth2/v2.0/authorize";

				return OAuth2AuthorizationRequest
						.authorizationCode()
						.clientId(azureB2CProperties.getCredential().getClientId())
						.authorizationUri(authorizationUri)
						.redirectUri(azureB2CProperties.getReplyUrl())
						.scope("openid")
						.additionalParameters(new HashMap<>() {{
							String codeVerifier = PkceUtil.generateCodeVerifier();
							String codeChallenge = null;
							try {
								codeChallenge = PkceUtil.generateCodeChallenge(codeVerifier);
								PkceUtil.savePkce("petstore-app", codeVerifier, codeChallenge);
							} catch (NoSuchAlgorithmException e) {
								throw new RuntimeException(e);
							}
							this.put("p", azureB2CProperties.getUserFlows().get("sign-up-or-sign-in"));
							this.put("nonce","defaultNonce");
							this.put("response_type","code");
							this.put("code_challenge", codeChallenge);
							this.put("code_challenge_method","S256");
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
	public AadB2cAuthorizationRequestResolver getAadB2cAuthorizationRequestResolver(AadB2cProperties properties, OAuth2AuthorizationRequestResolver resolver) {
		return new AadB2cAuthorizationRequestResolver(properties, resolver);
	}

	@Bean
	public LogoutSuccessHandler getAadB2cLogoutSuccessHandler(AadB2cProperties properties) {
		return new LogoutSuccessHandler(properties);
	}

	@Bean
	public AadB2cOidcLoginConfigurer getAadB2cOidcLoginConfigurer(LogoutSuccessHandler handler,
																  AadB2cAuthorizationRequestResolver resolver,
																  OAuth2AccessTokenResponseClient oAuth2AccessTokenResponseClient) {
		return new AadB2cOidcLoginConfigurer(handler, resolver, oAuth2AccessTokenResponseClient, new RestTemplateBuilder());
	}


	private JWTClaimsSet parseIdTokenClaims(String idToken) {
		if (idToken == null || idToken.isEmpty()) {
			throw new IllegalArgumentException("id_token is missing or empty");
		}

		try {
			// Parse and return JWT claims
			return JWTParser.parse(idToken).getJWTClaimsSet();
		} catch (Exception e) {
			throw new IllegalStateException("Failed to parse id_token: " + e.getMessage(), e);
		}
	}

	public static class PkceUtil {
		// private static final ThreadLocal<Map<String, String>> pkceStore = ThreadLocal.withInitial(HashMap::new);

		private static final ConcurrentHashMap<String, String> pkceStore = new ConcurrentHashMap<String, String>();
		// Generate a random code verifier
		public static String generateCodeVerifier() {
			SecureRandom secureRandom = new SecureRandom();
			byte[] randomBytes = new byte[32];
			secureRandom.nextBytes(randomBytes); // Populate with random data
			return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
		}

		// Generate SHA-256-based code challenge from the code verifier
		public static String generateCodeChallenge(String codeVerifier) throws NoSuchAlgorithmException {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashBytes = digest.digest(codeVerifier.getBytes());
			return Base64.getUrlEncoder().withoutPadding().encodeToString(hashBytes);
		}

		// Save code_verifier and code_challenge for a request
		public static void savePkce(String registrationId, String codeVerifier, String codeChallenge) {
			pkceStore.put(registrationId + "_code_verifier", codeVerifier);
			pkceStore.put(registrationId + "_code_challenge", codeChallenge);
		}

		// Retrieve the stored code_verifier
		public static String getCodeVerifier(String registrationId) {
			return pkceStore.get(registrationId + "_code_verifier");
		}

		// Retrieve the stored code_challenge
		public static String getCodeChallenge(String registrationId) {
			return pkceStore.get(registrationId + "_code_challenge");
		}
	}

	public static class TmpInMemoryClientRegistrationRepository implements ClientRegistrationRepository, Iterable<ClientRegistration> {

		private final Map<String, ClientRegistration> registrations = new HashMap<>();
		private final String registrationId = "petstore-app";

		public TmpInMemoryClientRegistrationRepository(ClientRegistration registrations) {
			this.registrations.put(registrationId, registrations);
		}

		public ClientRegistration findByRegistrationId(String registrationId) {
			return this.registrations.get(this.registrationId);
		}

		@Override
		public Iterator<ClientRegistration> iterator() {
			return this.registrations.values().iterator();
		}
	}

	public static class LogoutSuccessHandler extends AadB2cLogoutSuccessHandler {

		private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
		private final AadB2cProperties azureB2CProperties;

		public LogoutSuccessHandler(AadB2cProperties azureB2CProperties) {
			super(azureB2CProperties);
			this.azureB2CProperties = azureB2CProperties;
		}

		@Override
		public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

			String targetUrl = String.format("%s/oauth2/v2.0/logout?post_logout_redirect_uri=%s",
						getBaseRequest(azureB2CProperties) , azureB2CProperties.getLogoutSuccessUrl());

			if (response.isCommitted()) {
				this.logger.debug(LogMessage.format("Did not redirect to %s since response already committed.", targetUrl));
			} else {
				redirectStrategy.sendRedirect(request, response, targetUrl);
			}
		}
	}

	public class TokenUtils {

		public static JWTClaimsSet parseIdToken(String idToken) {
			try {
				return JWTParser.parse(idToken).getJWTClaimsSet();
			} catch (Exception e) {
				throw new IllegalArgumentException("Failed to parse JWT: " + e.getMessage(), e);
			}
		}
	}

}