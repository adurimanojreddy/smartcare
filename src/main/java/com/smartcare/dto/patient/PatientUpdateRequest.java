package com.smartcare.dto.patient;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** Patient self-service profile update (Module 4). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientUpdateRequest {

    @Size(max = 150)
    private String fullName;

    @Past
    private LocalDate dateOfBirth;

    private String gender;

    @Pattern(regexp = "^[+]?[0-9\\-\\s]{7,20}$")
    private String phone;

    @Size(max = 500)
    private String address;

    @Pattern(regexp = "^[+]?[0-9\\-\\s]{7,20}$")
    private String emergencyContact;

    @Size(max = 5)
    private String bloodGroup;

    private String profilePhotoUrl;
}
