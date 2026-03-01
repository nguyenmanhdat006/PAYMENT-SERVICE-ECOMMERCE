package com.ecommerce.paymentservice.exception;

import com.ecommerce.paymentservice.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ApiResponse<String>> handlePaymentException(PaymentException ex, WebRequest request) {
        ApiResponse<String> response = ApiResponse.<String>builder()
                .code(ex.getStatusCode())
                .message(ex.getMessage())
                .success(false)
                .build();

        return ResponseEntity.status(ex.getStatusCode()).body(response);
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

