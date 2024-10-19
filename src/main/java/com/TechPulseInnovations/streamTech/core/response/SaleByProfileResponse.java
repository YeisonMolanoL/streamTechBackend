package com.TechPulseInnovations.streamTech.core.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class SaleByProfileResponse {
    public String accountTypeName;
    public String clientName;
    public String clientNumber;
    public LocalDate profileSaleDueDate;
    public String profileSaleName;
    public String profileSalePin;
    public LocalDate profileSalePurchaseDate;
    public String accountEmail;
}
