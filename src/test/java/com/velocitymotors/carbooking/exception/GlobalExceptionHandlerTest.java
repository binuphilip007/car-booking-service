package com.velocitymotors.carbooking.exception;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void returnsFirstFieldErrorMessageForInvalidRequest() throws Exception {
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "bookingRequest");
        bindingResult.addError(new FieldError("bookingRequest", "vehicleId", "vehicleId must match the format VHnnnn"));

        ResponseEntity<ApiError> response = handler.handleValidation(
                new MethodArgumentNotValidException(methodParameter(), bindingResult));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("vehicleId must match the format VHnnnn", response.getBody().error());
    }

    @Test
    void returnsGenericMessageWhenNoFieldErrorIsPresent() throws Exception {
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "bookingRequest");

        ResponseEntity<ApiError> response = handler.handleValidation(
                new MethodArgumentNotValidException(methodParameter(), bindingResult));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid request", response.getBody().error());
    }

    @Test
    void propagatesStatusAndReasonFromResponseStatusException() {
        ResponseEntity<ApiError> response = handler.handleStatus(
                new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, "Credit-card payment was not approved"));

        assertEquals(HttpStatus.PAYMENT_REQUIRED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Credit-card payment was not approved", response.getBody().error());
    }

    @Test
    void mapsDataIntegrityViolationToConflict() {
        ResponseEntity<ApiError> response = handler.handleDataIntegrityViolation(
                new DataIntegrityViolationException("uk_bookings_payment_reference"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("paymentReference is already in use", response.getBody().error());
    }

    @Test
    void mapsUnreadableBodyToBadRequest() {
        ResponseEntity<ApiError> response = handler.handleMalformedRequest(
                new HttpMessageNotReadableException("broken",
                        new MockHttpInputMessage("{invalid-json".getBytes(StandardCharsets.UTF_8))));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Malformed request body", response.getBody().error());
    }

    private MethodParameter methodParameter() throws NoSuchMethodException {
        return new MethodParameter(GlobalExceptionHandlerTest.class.getDeclaredMethod("annotatedMethod", String.class), 0);
    }

    @SuppressWarnings("unused")
    private void annotatedMethod(String body) {
    }
}
