package com.smartcare.service;

import com.smartcare.dto.family.FamilyMemberRequest;
import com.smartcare.dto.family.FamilyMemberResponse;

import java.util.List;

public interface FamilyMemberService {

    List<FamilyMemberResponse> getByOwnerPatient(Long ownerPatientId);

    FamilyMemberResponse add(Long ownerPatientId, FamilyMemberRequest request);

    FamilyMemberResponse update(Long ownerPatientId, Long familyMemberId, FamilyMemberRequest request);

    void delete(Long ownerPatientId, Long familyMemberId);

    /** Throws UnauthorizedAccessException if the family member doesn't belong to this patient. */
    void assertOwnership(Long ownerPatientId, Long familyMemberId);
}
