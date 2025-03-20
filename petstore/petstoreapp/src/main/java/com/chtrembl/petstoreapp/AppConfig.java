package com.chtrembl.petstoreapp;

import com.chtrembl.petstoreapp.security.AADB2COidcLoginConfigurerWrapper;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.microsoft.applicationinsights.TelemetryClient;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableAutoConfiguration
@ComponentScan
@EnableCaching
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class AppConfig implements WebMvcConfigurer {

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
	}

	@Bean
	public AADB2COidcLoginConfigurerWrapper aadB2COidcLoginConfigurerWrapper() {
		return new AADB2COidcLoginConfigurerWrapper();
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
	public TelemetryClient telemetryClient() {
		System.out.println("Creating TelemetryClient Bean...");
		TelemetryClient telemetryClient = new TelemetryClient();
		telemetryClient.getContext().setInstrumentationKey("f88d6ca8-9929-4ed1-a815-0ec5d44473a3");
		return telemetryClient;
	}
}