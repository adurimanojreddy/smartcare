package com.smartcare.repository;

import com.smartcare.entity.FamilyMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FamilyMemberRepository extends JpaRepository<FamilyMember, Long> {

    List<FamilyMember> findByOwnerPatientId(Long ownerPatientId);
}
