package com.TechPulseInnovations.streamTech.core.request;

import lombok.Data;

import java.util.List;

@Data
public class MessageRequest {
    public String message;
    public List<String> vars;
    public List<SellByProfileRequest> sellByProfileRequests;
}
