package com.TechPulseInnovations.streamTech.codeReception.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Entidad que almacena solicitudes de códigos en long polling
 * Status: pending, assigned, expired
 */
@Entity
@Table(name = "code_requests", indexes = {
    @Index(name = "idx_email_status", columnList = "email, status"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_email_created", columnList = "email, created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CodeRequestStatus status = CodeRequestStatus.PENDING;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = true)
    private Long assignedCodeId;

    @Column(nullable = true)
    private LocalDateTime asignedAt;

    @Column(nullable = true)
    private LocalDateTime expiredAt;

    public enum CodeRequestStatus {
        PENDING,
        ASSIGNED,
        EXPIRED
    }
}
