package com.TechPulseInnovations.streamTech.app.modells;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "CLIENT_ENTITY")
@Data
public class ClientRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long clientId;
    public String name;
    public String contactNumber;
}
