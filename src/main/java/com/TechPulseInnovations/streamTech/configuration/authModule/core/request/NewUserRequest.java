package com.TechPulseInnovations.streamTech.configuration.authModule.core.request;

import lombok.Data;

@Data
public class NewUserRequest {
    private String name;
    private String lastName;
    private String userName;
    private String password;
}
