package com.TechPulseInnovations.streamTech.configuration.authModule.configuration.modells;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "ProfileSales")
@Data
public class ProfileSalesRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long profileSaleId;
    private String profileSaleName;
    private String profileSalePin;
    private LocalDate profileSaleDueDate;
    private LocalDate profileSalePurchaseDate;
    private boolean profileSaleStatus;

    @Column(nullable = false)
    private Integer profileSaleValidationAccess = 0;

    private String profileSaleType;
    @ManyToOne
    private ComboRecord profileComboRecord;
    @ManyToOne
    private AccountRecord accountRecord;
    @ManyToOne
    private ClientRecord clientRecord;
}
