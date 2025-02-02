package com.TechPulseInnovations.streamTech.configuration.authModule.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "USERS")
@Data
public class UserRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;
    public String name;
    public String lastName;
    @Column(unique = true, nullable = false)
    public String userName;
    public String password;
    public boolean enabled = true;
}
