package com.battja.accounting.exceptions;

public class BatchClosedException extends BookingException {

    public BatchClosedException(String errorMessage) {
        super(errorMessage);
    }
}
