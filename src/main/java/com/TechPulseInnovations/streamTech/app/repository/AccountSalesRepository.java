package com.TechPulseInnovations.streamTech.app.repository;

import com.TechPulseInnovations.streamTech.app.modells.AccountSalesRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountSalesRepository extends JpaRepository<AccountSalesRecord, Long> {
}
