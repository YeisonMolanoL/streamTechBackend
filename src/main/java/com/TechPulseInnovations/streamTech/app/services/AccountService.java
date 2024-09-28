package com.TechPulseInnovations.streamTech.app.services;

import com.TechPulseInnovations.streamTech.app.modells.AccountRecord;
import com.TechPulseInnovations.streamTech.app.modells.AccountTypeRecord;
import com.TechPulseInnovations.streamTech.app.repository.AccountRepository;
import com.TechPulseInnovations.streamTech.core.enums.AccountStatusSaleEnum;
import com.TechPulseInnovations.streamTech.core.errorException.ErrorMessages;
import com.TechPulseInnovations.streamTech.core.errorException.StreamTechException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountTypeService accountTypeService;
    private final I18NService i18NService;
    public AccountService(I18NService i18NService, AccountTypeService accountTypeService, AccountRepository accountRepository){
        this.accountRepository = accountRepository;
        this.accountTypeService = accountTypeService;
        this.i18NService = i18NService;
    }

    @Transactional(rollbackFor = Exception.class)
    public AccountRecord createAccount(AccountRecord accountRecord, long accountTypeId){
        log.info("AccountService:: createAccount accountRecord: [{}] accountTypeId: [{}]", accountRecord, accountTypeId);
        AccountTypeRecord accountTypeRecord = this.accountTypeService.getAccountTypeById(accountTypeId);
        accountTypeRecord.setAccountTypeAvailableProfiles(accountTypeRecord.getAccountTypeAvailableProfiles() + accountTypeRecord.getAccountTypeAmountProfile());
        accountRecord.setAccountTypeRecord(this.accountTypeService.getAccountTypeById(accountTypeId));
        accountRecord = this.accountRepository.save(accountRecord);
        this.accountTypeService.updateAccountType(accountTypeId, accountTypeRecord);
        return accountRecord;
    }

    public void updateAccount(long accountId, AccountRecord accountRecord) throws Exception {
        Optional<AccountRecord> accountRecordFound = this.accountRepository.findById(accountId);
        if(accountRecordFound.isEmpty()){
            throw new Exception("Error al encontrar la cuenta con id: {"+ accountId + "}");
        }
        accountRecordFound.get().setAccountTypeRecord(accountRecord.getAccountTypeRecord());
        accountRecordFound.get().setAccountAvailableProfiles(accountRecord.getAccountAvailableProfiles());
        accountRecordFound.get().setAccountEmail(accountRecord.getAccountEmail());
        accountRecordFound.get().setAccountPassword(accountRecord.getAccountPassword());
        accountRecordFound.get().setAccountDueDate(accountRecord.getAccountDueDate());
        accountRecordFound.get().setAccountPurchaseDate(accountRecord.getAccountPurchaseDate());
        accountRecordFound.get().setAccountStatusAcount(accountRecord.isAccountStatusAcount());
        accountRecordFound.get().setAccountStatusSale(accountRecord.isAccountStatusSale());
        accountRecordFound.get().setAccountProperty(accountRecord.isAccountProperty());
        this.accountRepository.save(accountRecordFound.get());
    }

    public AccountRecord getById(long accountId) {
        return this.accountRepository.findById(accountId).orElseThrow(() -> new StreamTechException(i18NService.getMessage(ErrorMessages.ACCOUNT_NOT_FOUND)));
    }

    public void softDelete(long accountId) throws Exception {
        AccountRecord accountRecord = this.accountRepository.findById(accountId).orElseThrow(() -> new Exception("Error al encontrar la cuenta con id: {"+ accountId + "}"));
        accountRecord.setAccountStatusAcount(!accountRecord.isAccountStatusAcount());
        this.accountRepository.save(accountRecord);
    }

    public List<AccountRecord> getAll(){
        return this.accountRepository.findAll();
    }

    public List<AccountRecord> getAllByType(long accountTypeId){
        AccountTypeRecord accountTypeRecord = this.accountTypeService.getAccountTypeById(accountTypeId);
        return this.accountRepository.findAllByAccountTypeRecord(accountTypeRecord);
    }

    public Page<AccountRecord> getAllByAccountType(AccountTypeRecord accountTypeRecord, int page, int pageSize){
        Pageable pageable = PageRequest.of(page, pageSize);
        return this.accountRepository.findAllByAccountTypeRecord(accountTypeRecord, pageable);
    }

    public Page<AccountRecord> getAllAvailableByAccountType(AccountTypeRecord accountTypeRecord, int page, int pageSize){
        Sort sort = Sort.by(Sort.Direction.ASC, "accountDueDate");
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        return this.accountRepository.findAllByAccountTypeRecordAndAccountAvailableProfilesAndAccountStatusSaleFalseAndAccountStatusAcountTrue(accountTypeRecord, pageable, accountTypeRecord.getAccountTypeAmountProfile());
    }

    public Page<AccountRecord> getAllAvailableByAccountTypeFilter(AccountTypeRecord accountTypeRecord, int page, int pageSize, boolean status){
        Sort sort = Sort.by(Sort.Direction.ASC, "accountDueDate");
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        return this.accountRepository.findAllByAccountTypeRecordAndAccountAvailableProfilesAndAccountStatusSaleFalseAndAccountStatusAcountTrueAndAccountProperty(accountTypeRecord, pageable, accountTypeRecord.getAccountTypeAmountProfile(), status);
    }

    @Transactional(rollbackFor = Exception.class)
    public void saleAccounts(List<AccountRecord> accountRecords){
        this.accountRepository.saveAll(accountRecords);
    }

    @Transactional(rollbackFor = Exception.class)
    public AccountRecord getAccountRecordsByDueDate(AccountTypeRecord accountTypeRecord) {
        log.info("AccountService:: getAccountRecordsByDueDate -> accountTypeRecord: [{}]", accountTypeRecord);
        LocalDate currentDate = LocalDate.now();
        System.out.println("Hasta aca llego");
        log.info("{}, {}, {}", accountTypeRecord, currentDate, 0);
        AccountRecord accountRecord = accountRepository.findFirstByAccountTypeRecordAndAccountDueDateAfterAndAccountAvailableProfilesGreaterThan(accountTypeRecord, currentDate, 0).orElseThrow(() -> new StreamTechException(i18NService.getMessage(ErrorMessages.ACCOUNT_PROFILE_AMOUNT_NOT_AVAILABLE)));
        System.out.println("Hasta aca llego normal");
        accountRecord.setAccountAvailableProfiles(accountRecord.getAccountAvailableProfiles() - 1);
        System.out.println("Hasta aca llego niormal 2");
        if(accountRecord.getAccountAvailableProfiles() == 0){
            accountRecord.setAccountStatusSale(true);
        }
        this.accountRepository.save(accountRecord);
        return accountRecord;
    }

    @Transactional(rollbackFor = Exception.class)
    public AccountRecord getAvailableByCombo(AccountTypeRecord accountTypeRecord){
        AccountRecord accountRecord = this.accountRepository.findFirstByAccountTypeRecordAndAccountStatusSaleIsFalseAndAccountAvailableProfilesNotAndAccountDueDateAfterOrderByAccountDueDateAsc(accountTypeRecord, 0, LocalDate.now()).orElseThrow(() -> new StreamTechException(i18NService.getMessage(ErrorMessages.ACCOUNT_NOT_AVAILABLE)));
        accountRecord.setAccountAvailableProfiles(accountRecord.getAccountAvailableProfiles() - 1);
        if(accountRecord.getAccountAvailableProfiles() == 0){
            accountRecord.setAccountStatusSale(true);
        }
        return this.accountRepository.save(accountRecord);
    }

    public AccountRecord getByEmail(String email){
        return this.accountRepository.findByAccountEmail(email).orElseThrow(() -> new StreamTechException(i18NService.getMessage(ErrorMessages.ACCOUNT_NOT_AVAILABLE)));
    }

    public void transfer(long oldAccount, long newAccount){
        log.info("AccountService:: transfer oldAccount: [{}], newAccount: [{}]", oldAccount, newAccount);
        AccountRecord oldAccountRecord = this.getById(oldAccount);
        AccountRecord newAccountRecord = this.getById(newAccount);
        oldAccountRecord.setAccountAvailableProfiles(oldAccountRecord.getAccountAvailableProfiles() + 1);
        newAccountRecord.setAccountAvailableProfiles(newAccountRecord.getAccountAvailableProfiles() - 1);
        log.info("AccountService:: transfer complete");
    }

    public Page<AccountRecord> getAccountsWithAvailableProfiles(int accountTypeId, int page, int pageSize){
        AccountTypeRecord accountTypeRecord = this.accountTypeService.getAccountTypeById(accountTypeId);
        Sort sort = Sort.by(Sort.Direction.ASC, "accountDueDate");
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        return this.accountRepository.findAllByAccountTypeRecordAndAccountAvailableProfilesGreaterThanEqual(accountTypeRecord, 1, pageable);
    }
}
