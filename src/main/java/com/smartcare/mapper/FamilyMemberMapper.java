package com.smartcare.mapper;

import com.smartcare.dto.family.FamilyMemberResponse;
import com.smartcare.entity.FamilyMember;

public final class FamilyMemberMapper {
    private FamilyMemberMapper() {}

    public static FamilyMemberResponse toResponse(FamilyMember f) {
        if (f == null) return null;
        return FamilyMemberResponse.builder()
                .id(f.getId())
                .fullName(f.getFullName())
                .relationship(f.getRelationship().name())
                .dateOfBirth(f.getDateOfBirth())
                .gender(f.getGender() != null ? f.getGender().name() : null)
                .bloodGroup(f.getBloodGroup())
                .build();
    }
}
