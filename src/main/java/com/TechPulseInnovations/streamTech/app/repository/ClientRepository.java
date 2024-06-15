package com.TechPulseInnovations.streamTech.app.repository;

import com.TechPulseInnovations.streamTech.app.modells.ClientRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<ClientRecord, Long> {
}
