package com.TechPulseInnovations.streamTech.configuration.authModule.models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

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
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<RolRecord> roles;
    public boolean enabled = true;
}
