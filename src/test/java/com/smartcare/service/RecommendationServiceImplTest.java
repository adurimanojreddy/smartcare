package com.smartcare.service;

import com.smartcare.dto.recommendation.DepartmentRecommendationResponse;
import com.smartcare.dto.recommendation.SlotRecommendationRequest;
import com.smartcare.dto.recommendation.SlotRecommendationResponse;
import com.smartcare.dto.recommendation.SymptomCheckRequest;
import com.smartcare.entity.AppointmentSlot;
import com.smartcare.entity.Doctor;
import com.smartcare.entity.Symptom;
import com.smartcare.entity.enums.SlotStatus;
import com.smartcare.repository.AppointmentSlotRepository;
import com.smartcare.repository.DepartmentRecommendationRepository;
import com.smartcare.repository.DepartmentRepository;
import com.smartcare.repository.DoctorRepository;
import com.smartcare.repository.SymptomRepository;
import com.smartcare.service.impl.RecommendationServiceImpl;
import static com.smartcare.TestEntities.withId;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/** Module 27 - smart recommendation engine (Modules 6/7), never a diagnosis. */
@ExtendWith(MockitoExtension.class)
class RecommendationServiceImplTest {

    @Mock private SymptomRepository symptomRepository;
    @Mock private DepartmentRecommendationRepository departmentRecommendationRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private DoctorRepository doctorRepository;
    @Mock private AppointmentSlotRepository slotRepository;

    @InjectMocks
    private RecommendationServiceImpl recommendationService;

    @Test
    void recommendDepartment_withUnrecognizedSymptoms_fallsBackToGeneralMedicine() {
        when(symptomRepository.findByNameIgnoreCase("made-up symptom")).thenReturn(Optional.empty());

        DepartmentRecommendationResponse response =
                recommendationService.recommendDepartment(new SymptomCheckRequest(List.of("made-up symptom")));

        assertThat(response.getRecommendedDepartmentName()).isEqualTo("General Medicine");
        assertThat(response.getDisclaimer()).containsIgnoringCase("not a medical diagnosis");
    }

    @Test
    void recommendDepartment_aggregatesWeightsAndPicksTopScoring() {
        Symptom rash = withId(Symptom.builder().name("skin rash").build(), 1L);
        when(symptomRepository.findByNameIgnoreCase("skin rash")).thenReturn(Optional.of(rash));

        // Dermatology scores higher than General Medicine.
        when(departmentRecommendationRepository.aggregateDepartmentScores(List.of(1L)))
                .thenReturn(List.of(new Object[]{2L, "Dermatology", 5L}, new Object[]{3L, "General Medicine", 1L}));

        when(doctorRepository.findByDepartmentIdAndStatus(2L, com.smartcare.entity.enums.DoctorStatus.ACTIVE))
                .thenReturn(List.of());

        DepartmentRecommendationResponse response =
                recommendationService.recommendDepartment(new SymptomCheckRequest(List.of("skin rash")));

        assertThat(response.getRecommendedDepartmentId()).isEqualTo(2L);
        assertThat(response.getRecommendedDepartmentName()).isEqualTo("Dermatology");
    }

    @Test
    void recommendSlots_prefersSlotWithinRequestedWindow() {
        Doctor doctor = withId(Doctor.builder().build(), 10L);
        when(doctorRepository.existsById(10L)).thenReturn(true);

        AppointmentSlot early = withId(AppointmentSlot.builder().doctor(doctor)
                .slotDate(LocalDate.now()).startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(9, 15))
                .status(SlotStatus.AVAILABLE).build(), 1L);
        AppointmentSlot preferred = withId(AppointmentSlot.builder().doctor(doctor)
                .slotDate(LocalDate.now()).startTime(LocalTime.of(10, 30)).endTime(LocalTime.of(10, 45))
                .status(SlotStatus.AVAILABLE).build(), 2L);

        when(slotRepository.findByDoctorIdAndSlotDateAndStatusOrderByStartTimeAsc(10L, LocalDate.now(), SlotStatus.AVAILABLE))
                .thenReturn(List.of(early, preferred));

        SlotRecommendationRequest request = SlotRecommendationRequest.builder()
                .doctorId(10L).preferredDate(LocalDate.now())
                .preferredStartTime(LocalTime.of(10, 0)).preferredEndTime(LocalTime.of(12, 0))
                .build();

        SlotRecommendationResponse response = recommendationService.recommendSlots(request);

        assertThat(response.getRecommendedSlot().getId()).isEqualTo(2L);
    }
}
