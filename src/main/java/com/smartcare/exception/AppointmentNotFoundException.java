package com.smartcare.exception;
public class AppointmentNotFoundException extends ResourceNotFoundException {
    public AppointmentNotFoundException(String message) { super(message); }
}
