package com.smartcare.repository;

import com.smartcare.entity.AppointmentSlot;
import com.smartcare.entity.Department;
import com.smartcare.entity.Doctor;
import com.smartcare.entity.User;
import com.smartcare.entity.enums.DoctorStatus;
import com.smartcare.entity.enums.Role;
import com.smartcare.entity.enums.SlotStatus;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Module 27 - "Repository tests where useful". Runs against the H2
 * in-memory database (see application-test.yml) so it exercises real JPA
 * mappings and constraints without needing MySQL.
 */
@DataJpaTest
@ActiveProfiles("test")
class AppointmentSlotRepositoryTest {

    @org.springframework.beans.factory.annotation.Autowired
    private AppointmentSlotRepository slotRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private DoctorRepository doctorRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private DepartmentRepository departmentRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private UserRepository userRepository;

    private Doctor createDoctor(String email) {
        User user = userRepository.save(User.builder()
                .email(email).password("hashed").role(Role.ROLE_DOCTOR).enabled(true).accountNonLocked(true).build());
        Department department = departmentRepository.save(Department.builder().name("Cardiology-" + email).active(true).build());
        return doctorRepository.save(Doctor.builder()
                .user(user).fullName("Dr. Test").specialization("Cardiology").department(department)
                .consultationDurationMinutes(15).status(DoctorStatus.ACTIVE).build());
    }

    @Test
    void uniqueConstraint_preventsDuplicateSlotForSameDoctorDateAndTime() {
        Doctor doctor = createDoctor("doc1@example.com");
        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime start = LocalTime.of(10, 0);

        slotRepository.save(AppointmentSlot.builder()
                .doctor(doctor).slotDate(date).startTime(start).endTime(start.plusMinutes(15))
                .status(SlotStatus.AVAILABLE).build());
        slotRepository.flush();

        assertThatThrownBy(() -> {
            slotRepository.save(AppointmentSlot.builder()
                    .doctor(doctor).slotDate(date).startTime(start).endTime(start.plusMinutes(15))
                    .status(SlotStatus.AVAILABLE).build());
            slotRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findByDoctorIdAndSlotDateAndStatus_returnsOnlyMatchingAvailableSlots() {
        Doctor doctor = createDoctor("doc2@example.com");
        LocalDate date = LocalDate.now().plusDays(2);

        slotRepository.save(AppointmentSlot.builder()
                .doctor(doctor).slotDate(date).startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(9, 15))
                .status(SlotStatus.AVAILABLE).build());
        slotRepository.save(AppointmentSlot.builder()
                .doctor(doctor).slotDate(date).startTime(LocalTime.of(9, 15)).endTime(LocalTime.of(9, 30))
                .status(SlotStatus.BOOKED).build());

        List<AppointmentSlot> available = slotRepository
                .findByDoctorIdAndSlotDateAndStatusOrderByStartTimeAsc(doctor.getId(), date, SlotStatus.AVAILABLE);

        assertThat(available).hasSize(1);
        assertThat(available.get(0).getStartTime()).isEqualTo(LocalTime.of(9, 0));
    }
}
