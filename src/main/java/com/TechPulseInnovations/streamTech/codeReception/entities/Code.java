package com.TechPulseInnovations.streamTech.codeReception.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Entidad que almacena códigos extraídos de correos
 */
@Entity
@Table(name = "codes", indexes = {
    @Index(name = "idx_email", columnList = "email"),
    @Index(name = "idx_is_assigned", columnList = "is_assigned"),
    @Index(name = "idx_email_assigned", columnList = "email, is_assigned")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Code {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isAssigned = false;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = true)
    private LocalDateTime assignedAt;

    @Column(nullable = true)
    private Long assignedToRequestId;
}
