package com.TechPulseInnovations.streamTech.app.services;

import com.TechPulseInnovations.streamTech.app.modells.AccountTypeRecord;
import com.TechPulseInnovations.streamTech.app.repository.AccountRepository;
import com.TechPulseInnovations.streamTech.app.repository.AccountTypeRepository;
import com.TechPulseInnovations.streamTech.core.errorException.ErrorMessages;
import com.TechPulseInnovations.streamTech.core.errorException.StreamTechException;
import com.TechPulseInnovations.streamTech.core.response.AccountTypeTotalResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AccountTypeService {
    private final AccountTypeRepository accountTypeRepository;
    private final AccountRepository accountRepository;

    public AccountTypeService(AccountRepository accountRepository, AccountTypeRepository accountTypeRepository){
        this.accountTypeRepository = accountTypeRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    public void createAccountType(AccountTypeRecord accountTypeRecord){
        this.accountTypeRepository.save(accountTypeRecord);
    }

    public void updateAccountType(long accountId, AccountTypeRecord accountTypeRecord) throws Exception {
        Optional<AccountTypeRecord> accountTypeRecordFound = this.accountTypeRepository.findById(accountId);
        if(accountTypeRecordFound.isEmpty()){
            throw new Exception("Error al encontrar la plataforma con id: {"+ accountId + "}");
        }
        accountTypeRecordFound.get().setAccountTypeName(accountTypeRecord.getAccountTypeName());
        accountTypeRecordFound.get().setAccountTypeAmountProfile(accountTypeRecord.getAccountTypeAmountProfile());
        accountTypeRecordFound.get().setAccountTypeStatus(accountTypeRecord.isAccountTypeStatus());
        this.accountTypeRepository.save(accountTypeRecordFound.get());
    }

    public AccountTypeRecord getAccountTypeById(long accountId) throws Exception {
        return this.accountTypeRepository.findById(accountId).orElseThrow(() -> new Exception("Error al encontrar la cuenta con id: {"+ accountId + "}"));
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
            throw new StreamTechException(ErrorMessages.ACCOUNTS_NOT_FOUND);
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
}
