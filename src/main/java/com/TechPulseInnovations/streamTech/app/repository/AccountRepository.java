package com.TechPulseInnovations.streamTech.app.repository;

import com.TechPulseInnovations.streamTech.app.modells.AccountRecord;
import com.TechPulseInnovations.streamTech.app.modells.AccountTypeRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<AccountRecord, Long> {
    Page<AccountRecord> findAllByAccountTypeRecordAndAccountAvailableProfilesGreaterThanEqual(AccountTypeRecord accountTypeRecord, int cantidad, Pageable pageable);
    Page<AccountRecord> findAllByAccountTypeRecord(AccountTypeRecord accountTypeRecord, Pageable pageable);
    int countByAccountTypeRecord(AccountTypeRecord accountTypeRecord);
    int countByAccountTypeRecordAndAccountPropertyTrue(AccountTypeRecord accountTypeRecord);
    int countByAccountTypeRecordAndAccountPropertyFalse(AccountTypeRecord accountTypeRecord);
    List<AccountRecord> findAllByAccountTypeRecord(AccountTypeRecord accountTypeRecord);

    Page<AccountRecord> findAllByAccountTypeRecordAndAccountAvailableProfilesAndAccountStatusSaleFalseAndAccountStatusAcountTrue(AccountTypeRecord accountTypeRecord, Pageable pageable, int profileAvailable);
    Page<AccountRecord> findAllByAccountTypeRecordAndAccountAvailableProfilesAndAccountStatusSaleFalseAndAccountStatusAcountTrueAndAccountProperty(AccountTypeRecord accountTypeRecord, Pageable pageable, int profileAvailable, boolean accountProperty);
    Optional<AccountRecord> findFirstByAccountTypeRecordAndAccountDueDateAfterAndAccountAvailableProfilesGreaterThan(AccountTypeRecord accountTypeRecord, LocalDate currentDate, int availableProfiles);
    Optional<AccountRecord> findFirstByAccountTypeRecordAndAccountStatusSaleIsFalseAndAccountAvailableProfilesNotAndAccountDueDateAfterOrderByAccountDueDateAsc(AccountTypeRecord accountTypeRecord, int availableProfiles, LocalDate currentDate);
    Optional<AccountRecord> findByAccountEmail(String email);
}
