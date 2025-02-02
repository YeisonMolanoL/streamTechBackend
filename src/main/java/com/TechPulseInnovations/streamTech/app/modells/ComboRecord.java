package com.TechPulseInnovations.streamTech.app.modells;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "COMBOS")
@Data
public class ComboRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long comboId;
    public String comboName;
    public String icon;
}
