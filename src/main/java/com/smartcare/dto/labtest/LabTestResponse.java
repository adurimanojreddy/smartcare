package com.smartcare.dto.labtest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabTestResponse {
    private Long id;
    private String testName;
    private String status;
    private LocalDate requestedDate;
    private String resultNotes;
    private String requestedByDoctorName;
}
