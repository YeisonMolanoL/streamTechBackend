package com.TechPulseInnovations.streamTech.app.modells;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "AccountType")
@Data
public class AccountTypeRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long accountTypeId;
    private String accountTypeName;
    private int accountTypeAmountProfile;
    private boolean accountTypeStatus;
    private String accountTypeIcon;
    private int accountTypeAmount;
    private int accountTypeAvailableProfiles;
}
