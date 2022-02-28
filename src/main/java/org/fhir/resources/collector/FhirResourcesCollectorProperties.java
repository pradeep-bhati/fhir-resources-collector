package org.fhir.resources.collector;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("fhir-resources-collector")
@Validated
@Component
@Primary
public class FhirResourcesCollectorProperties {
	/**
	 * no of threads in thread pool.
	 */
	private int corepoolsize = 10;
	/**
	 * max no of threads that can be created in thread pool.
	 */
	private int maxpoolsize = 20;
	/**
	 * size of queue for tasks to be added.
	 */
	private int queuecapacity = 100;
	/**
	 * uri to access each resource.
	 */
	private String baseURI = "http://10.70.30.101:3181/api/fhir";
	/**
	 * application will timeout after this time period.
	 */
	private long globalTimeOut = 6;
	/**
	 * no of retries before fallback.
	 */
	private int maxAttempts = 2;
	/**
	 * interval between retries.
	 */
	private int backoffInterval = 10;
	/**
	 * waiting time for connection to establish.
	 */
	private int connectionTimeOut = 500;
	/**
	 * waiting time for read to complete.
	 */
	private int readTimeOut = 500;
	
	public int getMaxAttempts() {
		return maxAttempts;
	}

	public void setMaxAttempts(int maxAttempts) {
		this.maxAttempts = maxAttempts;
	}

	public int getBackoffInterval() {
		return backoffInterval;
	}

	public void setBackoffInterval(int backoffInterval) {
		this.backoffInterval = backoffInterval;
	}

	public void setGlobalTimeOut(long globalTimeOut) {
		this.globalTimeOut = globalTimeOut;
	}

	public long getGlobalTimeOut() {
		return globalTimeOut;
	}

	public void setGlobalTimeOut(int globalTimeOut) {
		this.globalTimeOut = globalTimeOut;
	}

	public String getBaseURI() {
		return baseURI;
	}

	public void setBaseURI(String baseURI) {
		this.baseURI = baseURI;
	}

	public int getCorepoolsize() {
		return corepoolsize;
	}

	public void setCorepoolsize(int corepoolsize) {
		this.corepoolsize = corepoolsize;
	}

	public int getMaxpoolsize() {
		return maxpoolsize;
	}

	public void setMaxpoolsize(int maxpoolsize) {
		this.maxpoolsize = maxpoolsize;
	}

	public int getQueuecapacity() {
		return queuecapacity;
	}

	public void setQueuecapacity(int queuecapacity) {
		this.queuecapacity = queuecapacity;
	}

	public int getConnectionTimeOut() {
		return connectionTimeOut;
	}

	public void setConnectionTimeOut(int connectionTimeOut) {
		this.connectionTimeOut = connectionTimeOut;
	}

	public int getReadTimeOut() {
		return readTimeOut;
	}

	public void setReadTimeOut(int readTimeOut) {
		this.readTimeOut = readTimeOut;
	}

}
