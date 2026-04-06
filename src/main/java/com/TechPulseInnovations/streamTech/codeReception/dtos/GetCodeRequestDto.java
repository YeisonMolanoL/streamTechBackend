package com.TechPulseInnovations.streamTech.codeReception.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Solicitud para obtener código
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetCodeRequestDto {
    private String email;
}
