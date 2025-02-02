package com.TechPulseInnovations.streamTech.configuration.authModule.repository;

import com.TechPulseInnovations.streamTech.configuration.authModule.models.RolRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolRepository extends JpaRepository<RolRecord, Long> {
}
