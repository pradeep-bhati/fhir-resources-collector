package org.fhir.resources.collector.model;

/**
 * In case remote service responds with http status other than 200, this class
 * will be used to prepare response
 */
public class ExceptionResponse {

	private String status;
	private String reason;

	@Override
	public String toString() {
		return "ExceptionResponse [status=" + status + ", reason=" + reason + "]";
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

}
