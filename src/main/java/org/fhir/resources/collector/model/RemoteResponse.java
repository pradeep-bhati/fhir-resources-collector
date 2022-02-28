package org.fhir.resources.collector.model;

public class RemoteResponse {

	private String referencePathInJson;
	private String remoteResponse;
	/**
	 * to be set to true if any of remote services respond with exception
	 */
	private Boolean isRemoteServiceFail = false;

	public String getPath() {
		return referencePathInJson;
	}

	public void setPath(String referencePathInJson) {
		this.referencePathInJson = referencePathInJson;
	}

	public String getResponse() {
		return remoteResponse;
	}

	public void setResponse(String remoteResponse) {
		this.remoteResponse = remoteResponse;
	}

	public Boolean getRemoteServiceFail() {
		return isRemoteServiceFail;
	}

	public void setRemoteServiceFail(Boolean isRemoteServiceFail) {
		this.isRemoteServiceFail = isRemoteServiceFail;
	}

}
