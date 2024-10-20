package com.TechPulseInnovations.streamTech.core.request;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class SellByProfileRequest {
    public long accountTypeId;
    public String accountTypeName;
    public long clientId;
    public LocalDate profileSaleDueDate;
    public String profileSaleName;
    public String profileSalePin;
    public LocalDate profileSalePurchaseDate;
    public String message;
}
