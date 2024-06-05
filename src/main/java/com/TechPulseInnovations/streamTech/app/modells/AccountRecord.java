package com.TechPulseInnovations.streamTech.app.modells;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "Account")
@Data
public class AccountRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long accountId;
    private String accountEmail;
    private String accountPassword;
    private boolean accountStatusAcount;
    private boolean accountStatusSale;
    private boolean accountProperty;
    private Date accountDueDate;
    private Date accountPurchaseDate;
    private int accountAvailableProfiles;
    @ManyToOne
    private AccountTypeRecord accountTypeRecord;
}
