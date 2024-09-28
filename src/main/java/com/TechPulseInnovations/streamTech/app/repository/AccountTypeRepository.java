package com.TechPulseInnovations.streamTech.app.repository;

import com.TechPulseInnovations.streamTech.app.modells.AccountTypeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountTypeRepository extends JpaRepository<AccountTypeRecord, Long> {
    List<AccountTypeRecord> findAllByAccountTypeAvailableProfilesGreaterThan(int number);
    List<AccountTypeRecord> findByAccountTypeIdIn(List<Long> accountsTypeId);
}
