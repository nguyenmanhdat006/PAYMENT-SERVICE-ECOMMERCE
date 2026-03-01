package com.ecommerce.paymentservice.exception;

public class PaymentException extends RuntimeException {

    private String errorCode;
    private int statusCode;

    public PaymentException(String message) {
        super(message);
        this.statusCode = 400;
    }

    public PaymentException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.statusCode = 400;
    }

    public PaymentException(String message, String errorCode, int statusCode) {
        super(message);
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }

    public PaymentException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 400;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}

