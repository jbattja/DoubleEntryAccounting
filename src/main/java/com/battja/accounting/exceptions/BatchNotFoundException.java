package com.battja.accounting.exceptions;

public class BatchNotFoundException extends BookingException {

    public BatchNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
