package org.ishafoundation.dwaraapi.entrypoint.resource.controller.exception;

import java.util.Date;

import org.ishafoundation.dwaraapi.api.exception.DwaraException;
import org.ishafoundation.dwaraapi.entrypoint.resource.exception.ExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@RestController
public class DwaraResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
	
  @ExceptionHandler(DwaraException.class)
  public final ResponseEntity<ExceptionResponse> handleNotFoundException(DwaraException ex, WebRequest request) {
    ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), HttpStatus.BAD_REQUEST.value(), ex.getMessage(), request.getDescription(false), ex.getDetails());
    return new ResponseEntity<ExceptionResponse>(exceptionResponse, HttpStatus.BAD_REQUEST);
  }
}