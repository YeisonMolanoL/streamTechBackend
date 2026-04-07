package com.TechPulseInnovations.streamTech.app.repository;

import com.TechPulseInnovations.streamTech.configuration.authModule.configuration.modells.ClientRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<ClientRecord, Long> {
    Optional<ClientRecord> findByClientNumber(String clientNumber);
}
