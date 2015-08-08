package passio;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@ControllerAdvice
public class ErrorHandler {

	private static final Logger logger = Logger.getLogger("ErrorHandler");

	@ExceptionHandler
	@ResponseBody
	public ResponseEntity<String> clientErrorHandler(ClientError clientError) {
		ResponseStatus responseStatus = AnnotationUtils.findAnnotation(clientError.getClass(), ResponseStatus.class);
		return ResponseEntity
				.status(responseStatus.value())
				.body(String.format("%s (%s)", clientError.getCode(), clientError.getMessage()));
	}

	@ExceptionHandler
	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public String errorHandler(Exception exception) {
		String logref = UUID.randomUUID().toString();
		logger.log(Level.SEVERE, String.format("[%s] Unexpected exception", logref), exception);
		return String.format("Unknown_Error. Log_Ref=%s", logref);
	}

}
