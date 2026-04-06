package com.TechPulseInnovations.streamTech.codeReception.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Respuesta con el código obtenido
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CodeResponseDto {
    private Boolean success;
    private String code;
    private Long codeId;
    private Long requestId;
    private String message;
}
