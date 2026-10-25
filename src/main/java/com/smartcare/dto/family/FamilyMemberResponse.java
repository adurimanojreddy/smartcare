package com.smartcare.dto.family;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FamilyMemberResponse {
    private Long id;
    private String fullName;
    private String relationship;
    private LocalDate dateOfBirth;
    private String gender;
    private String bloodGroup;
}
