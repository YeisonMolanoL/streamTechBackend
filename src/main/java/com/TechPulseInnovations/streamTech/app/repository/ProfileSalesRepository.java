package com.TechPulseInnovations.streamTech.app.repository;

import com.TechPulseInnovations.streamTech.app.modells.AccountRecord;
import com.TechPulseInnovations.streamTech.app.modells.ProfileSalesRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfileSalesRepository extends JpaRepository<ProfileSalesRecord, Long> {
    List<ProfileSalesRecord> getAllByAccountRecord(AccountRecord accountRecord);
}
