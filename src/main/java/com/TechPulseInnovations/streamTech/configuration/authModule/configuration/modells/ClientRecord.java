package com.TechPulseInnovations.streamTech.configuration.authModule.configuration.modells;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "CLIENT_ENTITY")
@Data
public class ClientRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long clientId;
    public String clientName;
    @Column(unique = true)
    public String clientNumber;
}
