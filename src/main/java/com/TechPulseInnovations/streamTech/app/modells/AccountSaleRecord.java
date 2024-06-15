package com.TechPulseInnovations.streamTech.app.modells;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "AccountSale")
@Data
public class AccountSaleRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long accountSaleId;
    public LocalDate saleDate;
    public LocalDate dueDate;
    public String clientName;
    public LocalDate createAt;
    @ManyToOne
    public AccountRecord account;
    @ManyToOne
    public AccountTypeRecord accountTypeId;
    @ManyToOne
    public ClientRecord clientId;
}
