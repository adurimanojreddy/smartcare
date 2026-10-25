package com.smartcare.dto.family;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** Module 14 - add/update a family member (dependent) under a patient account. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FamilyMemberRequest {

    @NotBlank
    private String fullName;

    @NotNull
    private String relationship; // FATHER / MOTHER / SPOUSE / SON / DAUGHTER / ...

    @Past
    private LocalDate dateOfBirth;

    private String gender;

    private String bloodGroup;
}
