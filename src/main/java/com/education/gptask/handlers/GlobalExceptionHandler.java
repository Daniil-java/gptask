package com.education.gptask.handlers;

import com.education.gptask.entities.error.ErrorResponse;
import com.education.gptask.entities.error.ErrorResponseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDateTime;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler
    public ResponseEntity<ErrorResponse> catchAppException(ErrorResponseException e, WebRequest request) {
        log.error("Error occurred", e);
        return new ResponseEntity<>(
                new ErrorResponse()
                        .setStatus(e.getErrorStatus().getHttpStatus().value())
                        .setReasonPhrase(e.getErrorStatus().getHttpStatus().getReasonPhrase())
                        .setMessage(e.getErrorStatus().getMessage())
                        .setErrorCode(e.getErrorStatus())
                        .setAddress(ServletUriComponentsBuilder.fromCurrentRequest().toUriString())
                        .setCreated(LocalDateTime.now()),
                e.getErrorStatus().getHttpStatus());
    }
}
