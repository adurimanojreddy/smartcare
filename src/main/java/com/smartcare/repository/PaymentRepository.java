package com.smartcare.repository;

import com.smartcare.entity.Payment;
import com.smartcare.entity.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByAppointmentId(Long appointmentId);

    List<Payment> findByStatus(PaymentStatus status);

    // Admin revenue summary (Module 18).
    @Query("""
            SELECT COALESCE(SUM(p.amount), 0) FROM Payment p
            WHERE p.status = com.smartcare.entity.enums.PaymentStatus.SUCCESS
              AND p.paidAt >= :from AND p.paidAt < :to
            """)
    BigDecimal sumRevenueBetween(@Param("from") java.time.LocalDateTime from,
                                  @Param("to") java.time.LocalDateTime to);
}
