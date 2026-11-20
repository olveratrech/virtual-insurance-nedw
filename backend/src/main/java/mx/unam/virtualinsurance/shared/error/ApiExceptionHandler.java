package mx.unam.virtualinsurance.shared.error;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestControllerAdvice
public class ApiExceptionHandler {

	private static final String PROBLEM_BASE = "https://virtual-insurance.local/problems/";

	@ExceptionHandler(ApiException.class)
	ProblemDetail handleApiException(ApiException exception, HttpServletRequest request) {
		return createProblem(
			exception.getStatus(),
			exception.getTitle(),
			exception.getMessage(),
			exception.getCode(),
			exception.getProblemType(),
			request
		);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ProblemDetail handleValidation(
		MethodArgumentNotValidException exception,
		HttpServletRequest request
	) {
		List<FieldErrorResponse> fieldErrors = exception.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(error -> new FieldErrorResponse(error.getField(), error.getDefaultMessage()))
			.toList();

		ProblemDetail problem = createProblem(
			HttpStatus.UNPROCESSABLE_CONTENT,
			"La solicitud contiene datos invalidos",
			"Corrige los campos indicados.",
			"VALIDATION_ERROR",
			"validation-error",
			request
		);
		problem.setProperty("fieldErrors", fieldErrors);
		return problem;
	}

	@ExceptionHandler(MissingServletRequestPartException.class)
	ProblemDetail handleMissingPart(
		MissingServletRequestPartException exception,
		HttpServletRequest request
	) {
		return createProblem(
			HttpStatus.UNPROCESSABLE_CONTENT,
			"La solicitud multipart esta incompleta",
			"Falta la parte obligatoria: " + exception.getRequestPartName() + ".",
			"VALIDATION_ERROR",
			"validation-error",
			request
		);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	ProblemDetail handleUnreadableMessage(HttpServletRequest request) {
		return createProblem(
			HttpStatus.UNPROCESSABLE_CONTENT,
			"La solicitud contiene datos invalidos",
			"El contenido JSON no tiene el formato esperado.",
			"VALIDATION_ERROR",
			"validation-error",
			request
		);
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	ProblemDetail handleMaxUploadSize(HttpServletRequest request) {
		return createProblem(
			HttpStatus.UNPROCESSABLE_CONTENT,
			"La fotografia no es valida",
			"La fotografia supera el tamano permitido de 2 MiB.",
			"UNSUPPORTED_FILE",
			"unsupported-file",
			request
		);
	}

	private ProblemDetail createProblem(
		HttpStatus status,
		String title,
		String detail,
		String code,
		String problemType,
		HttpServletRequest request
	) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
		problem.setTitle(title);
		problem.setType(URI.create(PROBLEM_BASE + problemType));
		problem.setInstance(URI.create(request.getRequestURI()));
		problem.setProperty("code", code);
		problem.setProperty("timestamp", OffsetDateTime.now());
		return problem;
	}
}
