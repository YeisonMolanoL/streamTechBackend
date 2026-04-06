package com.TechPulseInnovations.streamTech.codeReception.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Solicitud para agregar una cuenta de email para monitoreo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddEmailAccountRequestDto {
    private String email;
    private String password;
    private String host;
    private Integer port;
    private Boolean secure;
}
