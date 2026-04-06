package com.TechPulseInnovations.streamTech.app.repository;

import com.TechPulseInnovations.streamTech.codeReception.entities.ParsingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParsingRuleRepository extends JpaRepository<ParsingRule, Long> {
    Optional<ParsingRule> findByServiceAndIsActiveTrue(String service);
    Optional<ParsingRule> findByService(String service);
}
