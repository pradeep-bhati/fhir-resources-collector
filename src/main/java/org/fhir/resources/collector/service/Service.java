package org.fhir.resources.collector.service;

import java.util.concurrent.CompletableFuture;

import org.fhir.resources.collector.model.RemoteResponse;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;

/**
 * Interface containing retry and recoverable method
 */
public interface Service {

	@Retryable(value = { RuntimeException.class }, maxAttempts = 3, backoff = @Backoff(delay = 1000))
	public CompletableFuture<RemoteResponse> getRemoteResponse(String path, String json);

	@Recover
	public CompletableFuture<RemoteResponse> prepareExceptionResponse(RuntimeException ex, String path, String json);
}
