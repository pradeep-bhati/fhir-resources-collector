package org.fhir.resources.collector.configuration;

import java.time.Duration;
import java.util.concurrent.Executor;

import org.fhir.resources.collector.FhirResourcesCollectorProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ThreadPoolConfig {

	@Autowired
	private FhirResourcesCollectorProperties fihrResourcesCollectorProperties;

	@Bean(name = "asyncExecutor")
	public Executor taskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(fihrResourcesCollectorProperties.getCorepoolsize());
		executor.setMaxPoolSize(fihrResourcesCollectorProperties.getMaxpoolsize());
		executor.setQueueCapacity(fihrResourcesCollectorProperties.getQueuecapacity());
		executor.setThreadNamePrefix("AsyncThread-");
		executor.initialize();
		return executor;
	}
	
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
		
		return restTemplateBuilder
		.setConnectTimeout(Duration.ofMillis(fihrResourcesCollectorProperties.getConnectionTimeOut()))
		.setReadTimeout(Duration.ofMillis(fihrResourcesCollectorProperties.getReadTimeOut()))
		.build();
	}
	
}
