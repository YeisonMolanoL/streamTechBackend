package com.TechPulseInnovations.streamTech.app.services;

import com.TechPulseInnovations.streamTech.app.modells.AccountTypeRecord;
import com.TechPulseInnovations.streamTech.app.repository.AccountRepository;
import com.TechPulseInnovations.streamTech.app.repository.AccountTypeRepository;
import com.TechPulseInnovations.streamTech.core.errorException.ErrorMessages;
import com.TechPulseInnovations.streamTech.core.errorException.StreamTechException;
import com.TechPulseInnovations.streamTech.core.response.AccountTypeTotalResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class AccountTypeService {
    private final AccountTypeRepository accountTypeRepository;
    private final AccountRepository accountRepository;
    private final I18NService i18NService;

    public AccountTypeService(I18NService i18NService, AccountRepository accountRepository, AccountTypeRepository accountTypeRepository){
        this.accountTypeRepository = accountTypeRepository;
        this.accountRepository = accountRepository;
        this.i18NService = i18NService;
    }

    @Transactional(rollbackFor = Exception.class)
    public void createAccountType(AccountTypeRecord accountTypeRecord){
        this.accountTypeRepository.save(accountTypeRecord);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateAccountType(long accountId, AccountTypeRecord accountTypeRecord) {
        log.info("AccountTypeService:: updateAccountType accountTypeRecord: {}", accountTypeRecord);
        AccountTypeRecord accountTypeRecordFound = this.accountTypeRepository.findById(accountId).orElseThrow(() -> new StreamTechException(i18NService.getMessage(ErrorMessages.ACCOUNT_TYPE_NOT_FOUND)));
        accountTypeRecordFound.setAccountTypeName(accountTypeRecord.getAccountTypeName());
        accountTypeRecordFound.setAccountTypeAmountProfile(accountTypeRecord.getAccountTypeAmountProfile());
        accountTypeRecordFound.setAccountTypeStatus(accountTypeRecord.isAccountTypeStatus());
        accountTypeRecordFound.setAccountTypeAvailableProfiles(accountTypeRecord.getAccountTypeAvailableProfiles());
        this.accountTypeRepository.save(accountTypeRecordFound);
    }

    public AccountTypeRecord getAccountTypeById(long accountId) {
        log.info("AccountTypeService:: getAccountTypeById -> accountId: [{}]", accountId);
        return this.accountTypeRepository.findById(accountId).orElseThrow(() -> new StreamTechException(i18NService.getMessage(ErrorMessages.ACCOUNT_TYPE_NOT_FOUND)));
    }

    public void softDeleteAccountType(long accountId) throws Exception {
        AccountTypeRecord accountTypeRecord = this.accountTypeRepository.findById(accountId).orElseThrow(() -> new Exception("Error al encontrar la cuenta con id: {"+ accountId + "}"));
        accountTypeRecord.setAccountTypeStatus(!accountTypeRecord.isAccountTypeStatus());
        this.accountTypeRepository.save(accountTypeRecord);
    }

    public List<AccountTypeRecord> getAll(){
        return this.accountTypeRepository.findAll();
    }

    public List<AccountTypeTotalResponse> getAccountTotalInformation() {
        List<AccountTypeRecord> accountsType = this.accountTypeRepository.findAll();
        AccountTypeTotalResponse accountTypeTotalResponse;
        List<AccountTypeTotalResponse> responseList = new ArrayList<>();
        if(accountsType.isEmpty()){
            throw new StreamTechException(i18NService.getMessage(ErrorMessages.ACCOUNTS_NOT_FOUND));
        }
        for (AccountTypeRecord accountTypeRecord: accountsType) {
            accountTypeTotalResponse = new AccountTypeTotalResponse();
            accountTypeTotalResponse.accountTypeRecord = accountTypeRecord;
            accountTypeTotalResponse.totalAccounts = this.accountRepository.countByAccountTypeRecord(accountTypeRecord);
            accountTypeTotalResponse.totalAccountsNoProperty = this.accountRepository.countByAccountTypeRecordAndAccountPropertyFalse(accountTypeRecord);
            accountTypeTotalResponse.totalAccountsProperty = this.accountRepository.countByAccountTypeRecordAndAccountPropertyTrue(accountTypeRecord);
            responseList.add(accountTypeTotalResponse);
        }
        return responseList;
    }

    public List<AccountTypeRecord> getAllWithAvailableProfile(){
        return this.accountTypeRepository.findAllByAccountTypeAvailableProfilesGreaterThan(0);
    }

    public List<AccountTypeRecord> getAccountsTypeRecords(List<Long> accountTypesId){
        return this.accountTypeRepository.findByAccountTypeIdIn(accountTypesId);
    }
}
