package com.TechPulseInnovations.streamTech.configuration.authModule.configuration.modells;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

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
    private String imapHost;
    private Integer imapPort;
    private Boolean imapSecure;
    private String connectionError;
    private Boolean isImapActive;
    @ManyToOne
    private AccountTypeRecord accountTypeRecord;
}
