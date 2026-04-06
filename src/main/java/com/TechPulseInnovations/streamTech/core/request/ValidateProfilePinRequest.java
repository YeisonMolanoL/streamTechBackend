package com.TechPulseInnovations.streamTech.core.request;

import lombok.Data;

/**
 * Solicitud para validar PIN de un ProfileSale antes de pedir el código
 */
@Data
public class ValidateProfilePinRequest {
    private long profileSaleId;
    private String email;
    private String pin;
}
