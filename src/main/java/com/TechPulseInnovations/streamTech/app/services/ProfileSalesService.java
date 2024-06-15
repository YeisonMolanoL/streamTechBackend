package com.TechPulseInnovations.streamTech.app.services;

import com.TechPulseInnovations.streamTech.app.modells.ProfileSalesRecord;
import com.TechPulseInnovations.streamTech.app.repository.ProfileSalesRepository;
import com.TechPulseInnovations.streamTech.core.errorException.ErrorMessages;
import com.TechPulseInnovations.streamTech.core.errorException.StreamTechException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ProfileSalesService {
    private final ProfileSalesRepository profileSalesRepository;

    public ProfileSalesService(ProfileSalesRepository profileSalesRepository){
        this.profileSalesRepository = profileSalesRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    public void createAccountSales(ProfileSalesRecord profileSalesRecord){
        this.profileSalesRepository.save(profileSalesRecord);
    }

    public void updateAccountSales(long accountSalesId, ProfileSalesRecord profileSalesRecord) throws Exception {
        Optional<ProfileSalesRecord> accountSalesRecordFound = this.profileSalesRepository.findById(accountSalesId);
        if(accountSalesRecordFound.isEmpty()){
            throw new StreamTechException(ErrorMessages.ACCOUNT_SALE_NOT_FOUND);
        }
        accountSalesRecordFound.get().setAccountRecord(profileSalesRecord.getAccountRecord());
        accountSalesRecordFound.get().setAccountSaleDueDate(profileSalesRecord.getAccountSaleDueDate());
        accountSalesRecordFound.get().setAccountSaleProfileName(profileSalesRecord.getAccountSaleProfileName());
        accountSalesRecordFound.get().setAccountSaleProfilePin(profileSalesRecord.getAccountSaleProfilePin());
        accountSalesRecordFound.get().setAccountSalePurchaseDate(profileSalesRecord.getAccountSalePurchaseDate());
        accountSalesRecordFound.get().setAccountSaleStatus(profileSalesRecord.isAccountSaleStatus());
        this.profileSalesRepository.save(accountSalesRecordFound.get());
    }

    public ProfileSalesRecord getAccountSalesById(long accountId) throws Exception {
        return this.profileSalesRepository.findById(accountId).orElseThrow(() -> new StreamTechException(ErrorMessages.ACCOUNT_SALE_NOT_FOUND));
    }

    public void softDeleteAccountSales(long accountId) throws Exception {
        ProfileSalesRecord profileSalesRecord = this.profileSalesRepository.findById(accountId).orElseThrow(() -> new StreamTechException(ErrorMessages.ACCOUNT_SALE_NOT_FOUND));
        profileSalesRecord.setAccountSaleStatus(!profileSalesRecord.isAccountSaleStatus());
        this.profileSalesRepository.save(profileSalesRecord);
    }
}
