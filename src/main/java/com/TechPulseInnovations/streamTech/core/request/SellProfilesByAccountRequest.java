package com.TechPulseInnovations.streamTech.core.request;

import lombok.Data;

import java.util.List;

@Data
public class SellProfilesByAccountRequest {
    public long accountTypeId;
    public long accountRecordId;
    public List<ProfileSaleRequest> profileSales;
}
