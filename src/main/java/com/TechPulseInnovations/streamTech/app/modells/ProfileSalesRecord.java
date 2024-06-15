package com.TechPulseInnovations.streamTech.app.modells;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "ProfileSales")
@Data
public class ProfileSalesRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long accountSaleId;
    private String accountSaleProfileName;
    private int accountSaleProfilePin;
    private Date accountSaleDueDate;
    private Date accountSalePurchaseDate;
    private boolean accountSaleStatus;
    @ManyToOne
    private AccountRecord accountRecord;
}