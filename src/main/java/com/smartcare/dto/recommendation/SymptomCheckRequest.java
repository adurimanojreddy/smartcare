package com.smartcare.dto.recommendation;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/** Module 6 - patient submits symptoms for department guidance. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SymptomCheckRequest {

    @NotEmpty(message = "Provide at least one symptom")
    private List<String> symptoms;
}
