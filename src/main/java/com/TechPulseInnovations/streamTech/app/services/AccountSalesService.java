package com.TechPulseInnovations.streamTech.app.services;

import com.TechPulseInnovations.streamTech.app.modells.AccountSalesRecord;
import com.TechPulseInnovations.streamTech.app.repository.AccountSalesRepository;
import com.TechPulseInnovations.streamTech.core.errorException.ErrorMessages;
import com.TechPulseInnovations.streamTech.core.errorException.StreamTechException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AccountSalesService {
    private final AccountSalesRepository accountSalesRepository;

    public AccountSalesService(AccountSalesRepository accountSalesRepository){
        this.accountSalesRepository = accountSalesRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    public void createAccountSales(AccountSalesRecord accountSalesRecord){
        this.accountSalesRepository.save(accountSalesRecord);
    }

    public void updateAccountSales(long accountSalesId, AccountSalesRecord accountSalesRecord) throws Exception {
        Optional<AccountSalesRecord> accountSalesRecordFound = this.accountSalesRepository.findById(accountSalesId);
        if(accountSalesRecordFound.isEmpty()){
            throw new StreamTechException(ErrorMessages.ACCOUNT_SALE_NOT_FOUND);
        }
        accountSalesRecordFound.get().setAccountRecord(accountSalesRecord.getAccountRecord());
        accountSalesRecordFound.get().setAccountSaleDueDate(accountSalesRecord.getAccountSaleDueDate());
        accountSalesRecordFound.get().setAccountSaleProfileName(accountSalesRecord.getAccountSaleProfileName());
        accountSalesRecordFound.get().setAccountSaleProfilePin(accountSalesRecord.getAccountSaleProfilePin());
        accountSalesRecordFound.get().setAccountSalePurchaseDate(accountSalesRecord.getAccountSalePurchaseDate());
        accountSalesRecordFound.get().setAccountSaleStatus(accountSalesRecord.isAccountSaleStatus());
        this.accountSalesRepository.save(accountSalesRecordFound.get());
    }

    public AccountSalesRecord getAccountSalesById(long accountId) throws Exception {
        return this.accountSalesRepository.findById(accountId).orElseThrow(() -> new StreamTechException(ErrorMessages.ACCOUNT_SALE_NOT_FOUND));
    }

    public void softDeleteAccountSales(long accountId) throws Exception {
        AccountSalesRecord accountSalesRecord = this.accountSalesRepository.findById(accountId).orElseThrow(() -> new StreamTechException(ErrorMessages.ACCOUNT_SALE_NOT_FOUND));
        accountSalesRecord.setAccountSaleStatus(!accountSalesRecord.isAccountSaleStatus());
        this.accountSalesRepository.save(accountSalesRecord);
    }
}
