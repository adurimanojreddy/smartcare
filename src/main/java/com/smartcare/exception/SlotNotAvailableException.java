package com.smartcare.exception;
import org.springframework.http.HttpStatus;
public class SlotNotAvailableException extends BaseException {
    public SlotNotAvailableException(String message) { super(message, HttpStatus.CONFLICT); }
}
