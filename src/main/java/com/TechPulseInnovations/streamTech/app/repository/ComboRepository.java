package com.TechPulseInnovations.streamTech.app.repository;

import com.TechPulseInnovations.streamTech.configuration.authModule.configuration.modells.ComboRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComboRepository extends JpaRepository<ComboRecord, Long> {
}
