package com.TechPulseInnovations.streamTech.configuration.authModule.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "ROLES")
@Data
public class RolRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;
    public String name;
    public boolean enabled;
}
