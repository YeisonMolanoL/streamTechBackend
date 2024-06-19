package com.TechPulseInnovations.streamTech.app.services;

import com.TechPulseInnovations.streamTech.app.modells.AccountRecord;
import com.TechPulseInnovations.streamTech.app.modells.AccountSaleRecord;
import com.TechPulseInnovations.streamTech.app.modells.AccountTypeRecord;
import com.TechPulseInnovations.streamTech.app.modells.ClientRecord;
import com.TechPulseInnovations.streamTech.app.repository.AccountSaleRepository;
import com.TechPulseInnovations.streamTech.core.request.SellByAccountRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class AccountSaleService {
    public final AccountSaleRepository accountSaleRepository;
    public final AccountTypeService accountTypeService;
    public final AccountService accountService;
    public final ClientService clientService;

    public AccountSaleService(AccountService accountService, ClientService clientService, AccountTypeService accountTypeService, AccountSaleRepository accountSaleRepository) {
        this.accountSaleRepository = accountSaleRepository;
        this.accountTypeService = accountTypeService;
        this.clientService = clientService;
        this.accountService = accountService;
    }

    @Transactional(rollbackFor = Exception.class)
    public void createAccountSale(SellByAccountRequest sellByAccountRequest){
        log.info("AccountSaleService:: createAccountSale");
        List<AccountTypeRecord> accountTypeRecords = new ArrayList<>();
        List<AccountSaleRecord> accountSaleRecords = new ArrayList<>();
        List<AccountRecord> accountRecords = new ArrayList<>();
        AccountSaleRecord accountSaleRecord;
        AccountTypeRecord accountTypeRecord;
        AccountRecord accountRecord;
        ClientRecord clientRecord;
        for (Long idAccount : sellByAccountRequest.getAccounts()) {
            accountSaleRecord = new AccountSaleRecord();
            accountRecord = this.accountService.getById(idAccount);
            accountTypeRecord = accountRecord.getAccountTypeRecord();
            accountTypeRecord.setAccountTypeAvailableProfiles(accountTypeRecord.getAccountTypeAvailableProfiles() - accountTypeRecord.getAccountTypeAmountProfile());
            clientRecord = this.clientService.getById(sellByAccountRequest.getClientId());
            accountRecord.setAccountStatusSale(true);
            accountRecord.setAccountAvailableProfiles(0);
            accountSaleRecord.setAccountTypeId(this.accountTypeService.getAccountTypeById(sellByAccountRequest.getAccountTypeId()));
            accountSaleRecord.setClientId(clientRecord);
            accountSaleRecord.setClientName(clientRecord.getName());
            accountSaleRecord.setDueDate(sellByAccountRequest.getDueDate());
            accountSaleRecord.setSaleDate(sellByAccountRequest.getSaleDate());
            accountSaleRecord.setAccount(accountRecord);
            accountSaleRecord.setCreateAt(LocalDate.now());
            accountSaleRecords.add(accountSaleRecord);
            accountRecords.add(accountRecord);
            this.accountTypeService.updateAccountType(accountTypeRecord.getAccountTypeId(), accountTypeRecord);
        }
        this.accountService.saleAccounts(accountRecords);
        this.accountSaleRepository.saveAll(accountSaleRecords);
    }
}
