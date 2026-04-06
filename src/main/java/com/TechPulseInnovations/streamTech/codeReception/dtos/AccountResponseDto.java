package com.TechPulseInnovations.streamTech.codeReception.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Respuesta con información de cuenta de email para administración
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountResponseDto {
    private Long id;
    private String email;
    private String imapHost;
    private Integer imapPort;
    private Boolean imapSecure;
    private Boolean isImapActive;
    private String connectionError;
    private LocalDate accountDueDate;
    private Boolean accountStatusAcount;
}