package com.smartcare.exception;
public class PatientNotFoundException extends ResourceNotFoundException {
    public PatientNotFoundException(String message) { super(message); }
}
