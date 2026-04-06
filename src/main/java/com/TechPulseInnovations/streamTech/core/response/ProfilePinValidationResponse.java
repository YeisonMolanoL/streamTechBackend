package com.TechPulseInnovations.streamTech.core.response;

import lombok.Data;

/**
 * Respuesta de validación de PIN para el perfil seleccionado
 */
@Data
public class ProfilePinValidationResponse {
    private boolean valid;
    private String message;
    private Integer profileSaleValidationAccess;
}
