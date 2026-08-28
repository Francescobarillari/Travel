package it.unical.ea.Travel.Exception;

import org.springframework.http.HttpStatus;

public class PaymentException extends ApiException {
    public PaymentException(String messageKey) {
        super(HttpStatus.BAD_REQUEST, messageKey);
    }

    public PaymentException(HttpStatus status, String messageKey) {
        super(status, messageKey);
    }
}
