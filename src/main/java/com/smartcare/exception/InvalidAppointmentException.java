package com.smartcare.exception;
import org.springframework.http.HttpStatus;
public class InvalidAppointmentException extends BaseException {
    public InvalidAppointmentException(String message) { super(message, HttpStatus.BAD_REQUEST); }
}
