package com.smartcare.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A single symptom keyword/phrase the patient can select or type, e.g.
 * "skin rash", "chest pain". Mapped to departments via
 * DepartmentRecommendation rather than a hardcoded if/else chain
 * (Module 6 - "configurable symptom-to-department mapping").
 */
@Getter
@Setter
@Entity
@Table(name = "symptoms")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Symptom extends BaseEntity {

    @NotBlank
    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}
