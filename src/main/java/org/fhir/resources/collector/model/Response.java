package org.fhir.resources.collector.model;

public class Response {

	private String jsonWithRefrenceData;
	/**
	 * to be set to true if any of remote services respond with exception
	 */
	private Boolean isExceptionOccured = false;

	public Boolean getIsExceptionOccured() {
		return isExceptionOccured;
	}

	public void setIsExceptionOccured(Boolean isExceptionOccured) {
		this.isExceptionOccured = isExceptionOccured;
	}

	public String getJsonWithRefrenceData() {
		return jsonWithRefrenceData;
	}

	public void setJsonWithRefrenceData(String jsonWithRefrenceData) {
		this.jsonWithRefrenceData = jsonWithRefrenceData;
	}

}
