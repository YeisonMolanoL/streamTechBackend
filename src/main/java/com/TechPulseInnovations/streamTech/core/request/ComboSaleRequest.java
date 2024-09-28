package com.TechPulseInnovations.streamTech.core.request;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ComboSaleRequest {
    public long existingCombo;
    public long clientId;
    public LocalDate profileSaleDueDate;
    public LocalDate profileSalePurchaseDate;
    public String comboName;
    public String profileSaleName;
    public String profileSalePin;
    public List<Long> comboAccountsType;
}
