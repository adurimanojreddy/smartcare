package com.smartcare.exception;
public class DoctorNotFoundException extends ResourceNotFoundException {
    public DoctorNotFoundException(String message) { super(message); }
}
