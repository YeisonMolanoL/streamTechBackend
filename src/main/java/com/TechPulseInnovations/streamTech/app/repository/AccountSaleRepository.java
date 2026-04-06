package com.TechPulseInnovations.streamTech.app.repository;

import com.TechPulseInnovations.streamTech.configuration.authModule.configuration.modells.AccountSaleRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountSaleRepository extends JpaRepository<AccountSaleRecord, Long> {
}
