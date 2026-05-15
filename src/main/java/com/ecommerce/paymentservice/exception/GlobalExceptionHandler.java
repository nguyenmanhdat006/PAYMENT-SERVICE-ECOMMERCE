package com.ecommerce.paymentservice.exception;

import com.ecommerce.paymentservice.dto.response.ApiResponse;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ApiResponse<String>> handlePaymentException(PaymentException ex, WebRequest request) {
        ApiResponse<String> response = ApiResponse.<String>builder()
                .code(ex.getStatusCode())
                .message(ex.getMessage())
                .success(false)
                .build();

        return ResponseEntity.status(HttpStatusCode.valueOf(ex.getStatusCode())).body(response);
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handlePaymentNotFound(PaymentNotFoundException ex) {
        ApiResponse<String> response = ApiResponse.<String>builder()
                .code(404)
                .message(ex.getMessage())
                .success(false)
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(InvalidSignatureException.class)
    public ResponseEntity<ApiResponse<String>> handleInvalidSignature(InvalidSignatureException ex) {
        ApiResponse<String> response = ApiResponse.<String>builder()
                .code(400)
                .message(ex.getMessage())
                .success(false)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<String>> handleValidation(MethodArgumentNotValidException ex) {
        String validationMessage = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ApiResponse<String> response = ApiResponse.<String>builder()
                .code(400)
                .message(validationMessage)
                .success(false)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGlobalException(Exception ex, WebRequest request) {
        ApiResponse<String> response = ApiResponse.<String>builder()
                .code(500)
                .message("Internal Server Error: " + ex.getMessage())
                .success(false)
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

