package org.fhir.resources.collector.service;

import java.util.concurrent.CompletableFuture;

import org.fhir.resources.collector.FhirResourcesCollectorProperties;
import org.fhir.resources.collector.model.Constants;
import org.fhir.resources.collector.model.ExceptionResponse;
import org.fhir.resources.collector.model.RemoteResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Recover;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

/**
 * class for calling remote service implementing retry and fallback logic
 */
@Component
public class RemoteServices implements Service{

	@Autowired
	private FhirResourcesCollectorProperties fhirResourcesCollectorProperties;

	@Autowired
	private RestTemplate restTemplate;
	/**
	 * calls remote services and retry in case of failures
	 */
	@Async("asyncExecutor")
	@Override
	public CompletableFuture<RemoteResponse> getRemoteResponse(String path, String json) {
		DocumentContext jsonContext = JsonPath.parse(json);
		String resourceUri = jsonContext.read(path);
		RemoteResponse requestResponse = new RemoteResponse();
		if (resourceUri.matches(Constants.REGEX)) {
			String uri = fhirResourcesCollectorProperties.getBaseURI() + Constants.FORWARD_SLASH + resourceUri;
			ResponseEntity<String> response = null;
			response = restTemplate.getForEntity(uri, String.class);
			requestResponse.setPath(path);
			requestResponse.setResponse(response.getBody());
		}
		return CompletableFuture.completedFuture(requestResponse);
	}

	/**
	 * Recovery logic if all the retries calls fails, prepare a response using
	 * ExceptionResponse
	 */
	@Recover
	@Override
	public CompletableFuture<RemoteResponse> prepareExceptionResponse(RuntimeException ex, String path, String json) {
		ExceptionResponse exceptionResponse = new ExceptionResponse();
		RemoteResponse requestResponse = new RemoteResponse();
		exceptionResponse.setStatus(Constants.STATUS);
		exceptionResponse.setReason(Constants.REMOTE_ERROR);
		ObjectMapper objectMapper = new ObjectMapper();
		String prepResponse = null;
		try {
			prepResponse = objectMapper.writeValueAsString(exceptionResponse);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		requestResponse.setPath(path);
		requestResponse.setResponse(prepResponse);
		requestResponse.setRemoteServiceFail(true);
		return CompletableFuture.completedFuture(requestResponse);
	}
}
