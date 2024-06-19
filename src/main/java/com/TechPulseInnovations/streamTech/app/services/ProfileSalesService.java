package com.TechPulseInnovations.streamTech.app.services;

import com.TechPulseInnovations.streamTech.app.modells.*;
import com.TechPulseInnovations.streamTech.app.repository.ProfileSalesRepository;
import com.TechPulseInnovations.streamTech.core.errorException.ErrorMessages;
import com.TechPulseInnovations.streamTech.core.errorException.StreamTechException;
import com.TechPulseInnovations.streamTech.core.request.SellByAccountRequest;
import com.TechPulseInnovations.streamTech.core.request.SellByProfileRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ProfileSalesService {
    private final ProfileSalesRepository profileSalesRepository;
    private final AccountTypeService accountTypeService;
    private final AccountService accountService;
    private final ClientService clientService;

    public ProfileSalesService(AccountTypeService accountTypeService, ClientService clientService, AccountService accountService, ProfileSalesRepository profileSalesRepository){
        this.profileSalesRepository = profileSalesRepository;
        this.accountTypeService = accountTypeService;
        this.accountService = accountService;
        this.clientService = clientService;
    }

    @Transactional(rollbackFor = Exception.class)
    public void createAccountSales(List<SellByProfileRequest> profilesSalesRequest){
        log.info("ProfileSalesService:: createAccountSales profilesSalesRequest: [{}]", profilesSalesRequest);
        List<ProfileSalesRecord> profileSalesRecords = new ArrayList<>();
        AccountTypeRecord accountTypeRecord;
        AccountRecord accountRecord;
        ProfileSalesRecord profileSalesRecord;
        for (SellByProfileRequest sellByAccountRequest : profilesSalesRequest) {
            accountTypeRecord = this.accountTypeService.getAccountTypeById(sellByAccountRequest.getAccountTypeId());
            accountTypeRecord.setAccountTypeAvailableProfiles(accountTypeRecord.getAccountTypeAvailableProfiles() - 1);
            accountRecord = this.accountService.getAccountRecordsByDueDate(accountTypeRecord);
            profileSalesRecord = new ProfileSalesRecord();
            profileSalesRecord.setProfileSaleName(sellByAccountRequest.getProfileSaleName());
            profileSalesRecord.setProfileSalePin(sellByAccountRequest.getProfileSalePin());
            profileSalesRecord.setProfileSaleDueDate(sellByAccountRequest.getProfileSaleDueDate());
            profileSalesRecord.setProfileSalePurchaseDate(profileSalesRecord.getProfileSaleDueDate());
            profileSalesRecord.setClientRecord(this.clientService.getById(sellByAccountRequest.getClientId()));
            profileSalesRecord.setAccountRecord(accountRecord);
            profileSalesRecords.add(profileSalesRecord);
            this.accountTypeService.updateAccountType(accountTypeRecord.getAccountTypeId(), accountTypeRecord);
        }
        this.profileSalesRepository.saveAll(profileSalesRecords);
    }

    public void updateAccountSales(long accountSalesId, ProfileSalesRecord profileSalesRecord) throws Exception {
        Optional<ProfileSalesRecord> accountSalesRecordFound = this.profileSalesRepository.findById(accountSalesId);
        if(accountSalesRecordFound.isEmpty()){
            throw new StreamTechException(ErrorMessages.ACCOUNT_SALE_NOT_FOUND);
        }
        accountSalesRecordFound.get().setAccountRecord(profileSalesRecord.getAccountRecord());
        accountSalesRecordFound.get().setProfileSaleDueDate(profileSalesRecord.getProfileSaleDueDate());
        accountSalesRecordFound.get().setProfileSaleName(profileSalesRecord.getProfileSaleName());
        accountSalesRecordFound.get().setProfileSalePin(profileSalesRecord.getProfileSalePin());
        accountSalesRecordFound.get().setProfileSalePurchaseDate(profileSalesRecord.getProfileSalePurchaseDate());
        accountSalesRecordFound.get().setProfileSaleStatus(profileSalesRecord.isProfileSaleStatus());
        this.profileSalesRepository.save(accountSalesRecordFound.get());
    }

    public ProfileSalesRecord getAccountSalesById(long accountId) {
        return this.profileSalesRepository.findById(accountId).orElseThrow(() -> new StreamTechException(ErrorMessages.ACCOUNT_SALE_NOT_FOUND));
    }

    public void softDeleteAccountSales(long accountId) {
        ProfileSalesRecord profileSalesRecord = this.profileSalesRepository.findById(accountId).orElseThrow(() -> new StreamTechException(ErrorMessages.ACCOUNT_SALE_NOT_FOUND));
        profileSalesRecord.setProfileSaleStatus(!profileSalesRecord.isProfileSaleStatus());
        this.profileSalesRepository.save(profileSalesRecord);
    }
}
