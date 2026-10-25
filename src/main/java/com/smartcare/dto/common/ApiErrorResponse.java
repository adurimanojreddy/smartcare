package com.smartcare.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Consistent error body returned by the global exception handler (Module 23).
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorResponse {

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    private int status;
    private String error;
    private String message;
    private String path;

    /** Field-level validation errors, e.g. {"email": "must not be blank"}. */
    private List<FieldErrorDetail> fieldErrors;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldErrorDetail {
        private String field;
        private String message;
    }
}
