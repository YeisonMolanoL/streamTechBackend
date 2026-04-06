package com.TechPulseInnovations.streamTech.core.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ProfileSaleSelectionResponse {
    private long profileSaleId;
    private String profileSaleName;
    private LocalDate profileSalePurchaseDate;
    private LocalDate profileSaleDueDate;
    private String profileSaleType;
    private boolean profileSaleStatus;
    private Integer profileSaleValidationAccess;
}
