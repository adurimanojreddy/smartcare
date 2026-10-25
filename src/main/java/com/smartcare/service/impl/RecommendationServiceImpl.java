package com.smartcare.service.impl;

import com.smartcare.dto.doctor.DoctorResponse;
import com.smartcare.dto.recommendation.DepartmentRecommendationResponse;
import com.smartcare.dto.recommendation.SlotRecommendationRequest;
import com.smartcare.dto.recommendation.SlotRecommendationResponse;
import com.smartcare.dto.recommendation.SymptomCheckRequest;
import com.smartcare.dto.slot.SlotResponse;
import com.smartcare.entity.AppointmentSlot;
import com.smartcare.entity.Doctor;
import com.smartcare.entity.Symptom;
import com.smartcare.entity.enums.DoctorStatus;
import com.smartcare.entity.enums.SlotStatus;
import com.smartcare.exception.DoctorNotFoundException;
import com.smartcare.exception.ResourceNotFoundException;
import com.smartcare.mapper.DoctorMapper;
import com.smartcare.mapper.SlotMapper;
import com.smartcare.repository.AppointmentSlotRepository;
import com.smartcare.repository.DepartmentRecommendationRepository;
import com.smartcare.repository.DepartmentRepository;
import com.smartcare.repository.DoctorRepository;
import com.smartcare.repository.SymptomRepository;
import com.smartcare.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Rule-based (NOT diagnostic) recommendation engine backing Module 6 and
 * Module 7. See Module 29: this must never be framed as medical diagnosis.
 */
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final SymptomRepository symptomRepository;
    private final DepartmentRecommendationRepository departmentRecommendationRepository;
    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentSlotRepository slotRepository;

    private static final int MAX_ALTERNATIVE_SLOTS = 3;

    @Override
    @Transactional(readOnly = true)
    public DepartmentRecommendationResponse recommendDepartment(SymptomCheckRequest request) {
        List<Long> symptomIds = request.getSymptoms().stream()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(name -> symptomRepository.findByNameIgnoreCase(name))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .map(Symptom::getId)
                .toList();

        if (symptomIds.isEmpty()) {
            // No recognized symptoms - fall back to a generic department
            // rather than guessing; still fully rule-based/data-driven.
            return DepartmentRecommendationResponse.builder()
                    .recommendedDepartmentId(null)
                    .recommendedDepartmentName("General Medicine")
                    .availableDoctors(List.of())
                    .build();
        }

        List<Object[]> scores = departmentRecommendationRepository.aggregateDepartmentScores(symptomIds);
        if (scores.isEmpty()) {
            return DepartmentRecommendationResponse.builder()
                    .recommendedDepartmentId(null)
                    .recommendedDepartmentName("General Medicine")
                    .availableDoctors(List.of())
                    .build();
        }

        // Highest aggregate weight wins (already ordered DESC by the query).
        Object[] top = scores.get(0);
        Long departmentId = (Long) top[0];
        String departmentName = (String) top[1];

        List<Doctor> doctors = doctorRepository.findByDepartmentIdAndStatus(departmentId, DoctorStatus.ACTIVE);
        List<DoctorResponse> doctorResponses = doctors.stream().map(DoctorMapper::toResponse).toList();

        LocalDateTime earliest = findEarliestAcrossDoctors(doctors);

        return DepartmentRecommendationResponse.builder()
                .recommendedDepartmentId(departmentId)
                .recommendedDepartmentName(departmentName)
                .availableDoctors(doctorResponses)
                .earliestAvailableSlot(earliest)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SlotRecommendationResponse recommendSlots(SlotRecommendationRequest request) {
        if (!doctorRepository.existsById(request.getDoctorId())) {
            throw new DoctorNotFoundException("Doctor not found with id: " + request.getDoctorId());
        }

        List<AppointmentSlot> available = slotRepository.findByDoctorIdAndSlotDateAndStatusOrderByStartTimeAsc(
                request.getDoctorId(), request.getPreferredDate(), SlotStatus.AVAILABLE);

        if (available.isEmpty()) {
            return SlotRecommendationResponse.builder()
                    .recommendedSlot(null)
                    .alternativeSlots(List.of())
                    .build();
        }

        LocalTime preferredStart = request.getPreferredStartTime();
        LocalTime preferredEnd = request.getPreferredEndTime();

        List<AppointmentSlot> withinWindow = new ArrayList<>();
        if (preferredStart != null) {
            LocalTime windowEnd = preferredEnd != null ? preferredEnd : LocalTime.MAX;
            for (AppointmentSlot slot : available) {
                if (!slot.getStartTime().isBefore(preferredStart) && !slot.getStartTime().isAfter(windowEnd)) {
                    withinWindow.add(slot);
                }
            }
        }

        // Prefer a slot inside the patient's requested window; otherwise
        // fall back to the earliest available slot that day (Module 7).
        List<AppointmentSlot> candidates = !withinWindow.isEmpty() ? withinWindow : available;
        candidates = candidates.stream()
                .sorted(Comparator.comparing(AppointmentSlot::getStartTime))
                .collect(Collectors.toList());

        AppointmentSlot recommended = candidates.get(0);
        List<SlotResponse> alternatives = candidates.stream()
                .skip(1)
                .limit(MAX_ALTERNATIVE_SLOTS)
                .map(SlotMapper::toResponse)
                .toList();

        return SlotRecommendationResponse.builder()
                .recommendedSlot(SlotMapper.toResponse(recommended))
                .alternativeSlots(alternatives)
                .build();
    }

    private LocalDateTime findEarliestAcrossDoctors(List<Doctor> doctors) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        LocalDateTime best = null;
        for (Doctor doctor : doctors) {
            List<AppointmentSlot> earliest = slotRepository.findEarliestAvailable(doctor.getId(), today, now);
            if (!earliest.isEmpty()) {
                AppointmentSlot slot = earliest.get(0);
                LocalDateTime candidate = LocalDateTime.of(slot.getSlotDate(), slot.getStartTime());
                if (best == null || candidate.isBefore(best)) {
                    best = candidate;
                }
            }
        }
        return best;
    }
}
