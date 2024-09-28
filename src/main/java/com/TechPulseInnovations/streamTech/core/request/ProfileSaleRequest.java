package com.TechPulseInnovations.streamTech.core.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ProfileSaleRequest {
    public long clientId;
    public LocalDate profileSaleDueDate;
    public String profileSaleName;
    public String profileSalePin;
    public LocalDate profileSalePurchaseDate;
}
