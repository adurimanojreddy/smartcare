package com.smartcare.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Maps a Symptom to a Department with a weight/priority, driving the
 * rule-based (NOT diagnostic) department recommendation engine in Module 6.
 * Kept as data rather than code so admins can extend it via the DB/admin UI
 * without a redeploy.
 */
@Getter
@Setter
@Entity
@Table(
        name = "department_recommendations",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_symptom_department",
                columnNames = {"symptom_id", "department_id"}
        )
)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class DepartmentRecommendation extends BaseEntity {

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "symptom_id", nullable = false)
    private Symptom symptom;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    // Higher weight = stronger signal for this department when the
    // symptom is present. Lets multiple symptoms contribute a combined
    // score instead of a single hardcoded match.
    @Column(nullable = false)
    @Builder.Default
    private Integer weight = 1;
}
