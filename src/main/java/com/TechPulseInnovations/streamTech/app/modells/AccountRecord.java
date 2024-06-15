package com.TechPulseInnovations.streamTech.app.modells;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
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
    private LocalDate accountDueDate;
    private LocalDate accountPurchaseDate;
    private int accountAvailableProfiles;
    @ManyToOne
    private AccountTypeRecord accountTypeRecord;
}
