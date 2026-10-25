package com.smartcare.service.impl;

import com.smartcare.dto.family.FamilyMemberRequest;
import com.smartcare.dto.family.FamilyMemberResponse;
import com.smartcare.entity.FamilyMember;
import com.smartcare.entity.Patient;
import com.smartcare.entity.enums.FamilyRelationship;
import com.smartcare.entity.enums.Gender;
import com.smartcare.exception.PatientNotFoundException;
import com.smartcare.exception.ResourceNotFoundException;
import com.smartcare.exception.UnauthorizedAccessException;
import com.smartcare.mapper.FamilyMemberMapper;
import com.smartcare.repository.FamilyMemberRepository;
import com.smartcare.repository.PatientRepository;
import com.smartcare.service.FamilyMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Module 14 - Family Account. */
@Service
@RequiredArgsConstructor
public class FamilyMemberServiceImpl implements FamilyMemberService {

    private final FamilyMemberRepository familyMemberRepository;
    private final PatientRepository patientRepository;

    @Override
    @Transactional(readOnly = true)
    public List<FamilyMemberResponse> getByOwnerPatient(Long ownerPatientId) {
        return familyMemberRepository.findByOwnerPatientId(ownerPatientId).stream()
                .map(FamilyMemberMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public FamilyMemberResponse add(Long ownerPatientId, FamilyMemberRequest request) {
        Patient owner = patientRepository.findById(ownerPatientId)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found with id: " + ownerPatientId));

        FamilyMember member = FamilyMember.builder()
                .ownerPatient(owner)
                .fullName(request.getFullName())
                .relationship(FamilyRelationship.valueOf(request.getRelationship().toUpperCase()))
                .dateOfBirth(request.getDateOfBirth())
                .gender(parseGender(request.getGender()))
                .bloodGroup(request.getBloodGroup())
                .build();

        return FamilyMemberMapper.toResponse(familyMemberRepository.save(member));
    }

    @Override
    @Transactional
    public FamilyMemberResponse update(Long ownerPatientId, Long familyMemberId, FamilyMemberRequest request) {
        FamilyMember member = findOrThrow(familyMemberId);
        assertOwnership(ownerPatientId, familyMemberId, member);

        if (request.getFullName() != null) member.setFullName(request.getFullName());
        if (request.getRelationship() != null) {
            member.setRelationship(FamilyRelationship.valueOf(request.getRelationship().toUpperCase()));
        }
        if (request.getDateOfBirth() != null) member.setDateOfBirth(request.getDateOfBirth());
        if (request.getGender() != null) member.setGender(parseGender(request.getGender()));
        if (request.getBloodGroup() != null) member.setBloodGroup(request.getBloodGroup());

        return FamilyMemberMapper.toResponse(familyMemberRepository.save(member));
    }

    @Override
    @Transactional
    public void delete(Long ownerPatientId, Long familyMemberId) {
        FamilyMember member = findOrThrow(familyMemberId);
        assertOwnership(ownerPatientId, familyMemberId, member);
        familyMemberRepository.delete(member);
    }

    @Override
    @Transactional(readOnly = true)
    public void assertOwnership(Long ownerPatientId, Long familyMemberId) {
        assertOwnership(ownerPatientId, familyMemberId, findOrThrow(familyMemberId));
    }

    private void assertOwnership(Long ownerPatientId, Long familyMemberId, FamilyMember member) {
        if (!member.getOwnerPatient().getId().equals(ownerPatientId)) {
            throw new UnauthorizedAccessException(
                    "Family member " + familyMemberId + " does not belong to this patient account");
        }
    }

    private FamilyMember findOrThrow(Long familyMemberId) {
        return familyMemberRepository.findById(familyMemberId)
                .orElseThrow(() -> new ResourceNotFoundException("Family member not found with id: " + familyMemberId));
    }

    private Gender parseGender(String gender) {
        if (gender == null || gender.isBlank()) return null;
        try {
            return Gender.valueOf(gender.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
