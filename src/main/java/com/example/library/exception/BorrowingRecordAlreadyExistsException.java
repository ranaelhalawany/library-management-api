package com.example.library.exception;

public class BorrowingRecordAlreadyExistsException extends RuntimeException {
    public BorrowingRecordAlreadyExistsException(String message) {
        super(message);
    }
}
