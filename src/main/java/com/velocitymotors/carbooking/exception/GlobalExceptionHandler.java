package com.velocitymotors.carbooking.exception;

import com.velocitymotors.carbooking.model.entity.PaymentMode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;
import tools.jackson.databind.exc.InvalidFormatException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Invalid request");
        log.warn("Request validation failed: {}", message);
        return ResponseEntity.badRequest().body(new ApiError(message));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleStatus(ResponseStatusException exception) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        log.warn("Request failed status={} reason={}", status.value(), exception.getReason());
        return ResponseEntity.status(status).body(new ApiError(exception.getReason()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(DataIntegrityViolationException exception) {
        log.warn("Request violated a database constraint", exception);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError("paymentReference is already in use"));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleMalformedRequest(HttpMessageNotReadableException exception) {
        if (exception.getCause() instanceof InvalidFormatException invalidFormatException
                && invalidFormatException.getTargetType() == PaymentMode.class) {
            String message = "paymentMode must be one of: "
                    + String.join(", ", java.util.Arrays.stream(PaymentMode.values())
                    .map(Enum::name)
                    .toList());
            log.warn("Request validation failed: {}", message);
            return ResponseEntity.badRequest().body(new ApiError(message));
        }
        log.warn("Malformed request body", exception);
        return ResponseEntity.badRequest().body(new ApiError("Malformed request body"));
    }
}