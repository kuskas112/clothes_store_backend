package com.example.clothesstore.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Регрессия: раньше @ExceptionHandler(Exception.class) перехватывал
 * ResponseStatusException и превращал 404/409 в 500.
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void responseStatusExceptionKeepsItsStatus() {
        var response = handler.handleResponseStatus(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo(new ErrorResponse("Product not found"));
    }

    @Test
    void responseStatusExceptionWithoutReasonDoesNotReturn500() {
        var response = handler.handleResponseStatus(
                new ResponseStatusException(HttpStatus.CONFLICT));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void uniqueConstraintViolationBecomesConflict() {
        var response = handler.handleDataIntegrity(
                new DataIntegrityViolationException("duplicate key", new RuntimeException("uk_users_email")));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void unexpectedExceptionStillReturns500() {
        var response = handler.handleGeneric(new IllegalStateException("boom"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
