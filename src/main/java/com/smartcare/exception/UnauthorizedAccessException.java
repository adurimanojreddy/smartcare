package com.smartcare.exception;
import org.springframework.http.HttpStatus;
public class UnauthorizedAccessException extends BaseException {
    public UnauthorizedAccessException(String message) { super(message, HttpStatus.FORBIDDEN); }
}
