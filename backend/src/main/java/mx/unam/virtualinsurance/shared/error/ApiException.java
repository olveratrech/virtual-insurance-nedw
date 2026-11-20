package mx.unam.virtualinsurance.shared.error;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {

	private final HttpStatus status;
	private final String code;
	private final String title;
	private final String problemType;

	public ApiException(
		HttpStatus status,
		String code,
		String title,
		String detail,
		String problemType
	) {
		super(detail);
		this.status = status;
		this.code = code;
		this.title = title;
		this.problemType = problemType;
	}

	public HttpStatus getStatus() {
		return status;
	}

	public String getCode() {
		return code;
	}

	public String getTitle() {
		return title;
	}

	public String getProblemType() {
		return problemType;
	}
}
