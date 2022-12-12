package com.tokenexchange.infrastructure;


import com.tokenexchange.infrastructure.exception.ErrorResponse;
import com.tokenexchange.infrastructure.exception.ErrorType;
import com.tokenexchange.infrastructure.exception.ParameterizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Collections;

@RestControllerAdvice(annotations = {
	RestController.class
})
public class GlobalRestControllerAdvice {
	private final Logger LOGGER = LoggerFactory.getLogger(GlobalRestControllerAdvice.class);


	@ExceptionHandler(ParameterizedException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	ErrorResponse handleWebParameterizedException(ParameterizedException parameterizedException) {
		LOGGER.error(parameterizedException.getCode().value(), parameterizedException.getParams());
		return new ErrorResponse(parameterizedException.getCode(), parameterizedException.getParams());
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	ErrorResponse handleException(Exception exception)  {
		LOGGER.error(exception.toString(), exception);
		return new ErrorResponse(ErrorType.INTERNAL_ERROR, Collections.emptyMap());
	}
}
