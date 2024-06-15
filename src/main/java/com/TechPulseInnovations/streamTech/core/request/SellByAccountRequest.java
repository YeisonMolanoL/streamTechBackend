package com.TechPulseInnovations.streamTech.core.request;

import lombok.Data;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Data
public class SellByAccountRequest {
    public long accountTypeId;
    public long clientId;
    public LocalDate saleDate;
    public LocalDate dueDate;
    public List<Long> accounts;
}
