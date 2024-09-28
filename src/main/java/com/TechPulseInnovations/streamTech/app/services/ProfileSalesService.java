package com.TechPulseInnovations.streamTech.app.services;

import com.TechPulseInnovations.streamTech.app.modells.*;
import com.TechPulseInnovations.streamTech.app.repository.ProfileSalesRepository;
import com.TechPulseInnovations.streamTech.core.chat.ChatConnection;
import com.TechPulseInnovations.streamTech.core.enums.ProfileSaleType;
import com.TechPulseInnovations.streamTech.core.errorException.ErrorMessages;
import com.TechPulseInnovations.streamTech.core.errorException.StreamTechException;
import com.TechPulseInnovations.streamTech.core.request.ProfileSaleRequest;
import com.TechPulseInnovations.streamTech.core.request.SellByProfileRequest;
import com.TechPulseInnovations.streamTech.core.request.SellProfilesByAccountRequest;
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
    private final ChatConnection chatConnection;
    private final AccountService accountService;
    private final ClientService clientService;
    private final I18NService i18NService;


    public ProfileSalesService(ChatConnection chatConnection, I18NService i18NService, AccountTypeService accountTypeService, ClientService clientService, AccountService accountService, ProfileSalesRepository profileSalesRepository){
        this.profileSalesRepository = profileSalesRepository;
        this.accountTypeService = accountTypeService;
        this.accountService = accountService;
        this.chatConnection = chatConnection;
        this.clientService = clientService;
        this.i18NService = i18NService;
    }

    @Transactional(rollbackFor = Exception.class)
    public void createProfileSale(List<SellByProfileRequest> profilesSalesRequest){
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
            profileSalesRecord.setProfileSalePurchaseDate(sellByAccountRequest.getProfileSaleDueDate());
            profileSalesRecord.setClientRecord(this.clientService.getById(sellByAccountRequest.getClientId()));
            profileSalesRecord.setAccountRecord(accountRecord);
            profileSalesRecord.setProfileSaleType(ProfileSaleType.UNIDAD.name());
            profileSalesRecords.add(profileSalesRecord);
            this.profileSalesRepository.save(profileSalesRecord);
            this.chatConnection.sendMessage("Hola querido Yeison tu perfil de ".concat(accountTypeRecord.getAccountTypeName()).concat(" quedo con el nombre ".
                    concat(profileSalesRecord.getProfileSaleName()).concat(" y con el pin ").concat(profileSalesRecord.getProfileSalePin())
                    .concat(" recuerda que vence el dia ").concat(profileSalesRecord.getProfileSaleDueDate().toString())), "3209851930");
            this.accountTypeService.updateAccountType(accountTypeRecord.getAccountTypeId(), accountTypeRecord);
        }
        //this.profileSalesRepository.saveAll(profileSalesRecords);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateProfileSale(long accountSalesId, ProfileSalesRecord profileSalesRecord) {
        Optional<ProfileSalesRecord> accountSalesRecordFound = this.profileSalesRepository.findById(accountSalesId);
        if(accountSalesRecordFound.isEmpty()){
            throw new StreamTechException(i18NService.getMessage(ErrorMessages.ACCOUNT_SALE_NOT_FOUND));
        }
        accountSalesRecordFound.get().setAccountRecord(profileSalesRecord.getAccountRecord());
        accountSalesRecordFound.get().setProfileSaleDueDate(profileSalesRecord.getProfileSaleDueDate());
        accountSalesRecordFound.get().setProfileSaleName(profileSalesRecord.getProfileSaleName());
        accountSalesRecordFound.get().setProfileSalePin(profileSalesRecord.getProfileSalePin());
        accountSalesRecordFound.get().setProfileSalePurchaseDate(profileSalesRecord.getProfileSalePurchaseDate());
        accountSalesRecordFound.get().setProfileSaleStatus(profileSalesRecord.isProfileSaleStatus());
        this.profileSalesRepository.save(accountSalesRecordFound.get());
    }

    public ProfileSalesRecord getProfileSaleById(long accountId) {
        return this.profileSalesRepository.findById(accountId).orElseThrow(() -> new StreamTechException(i18NService.getMessage(ErrorMessages.ACCOUNT_SALE_NOT_FOUND)));
    }

    @Transactional(rollbackFor = Exception.class)
    public void softDeleteAccountSales(long accountId) {
        ProfileSalesRecord profileSalesRecord = this.profileSalesRepository.findById(accountId).orElseThrow(() -> new StreamTechException(i18NService.getMessage(ErrorMessages.ACCOUNT_SALE_NOT_FOUND)));
        profileSalesRecord.setProfileSaleStatus(!profileSalesRecord.isProfileSaleStatus());
        this.profileSalesRepository.save(profileSalesRecord);
    }

    @Transactional(rollbackFor = Exception.class)
    public void createComboProfilesSales(ComboRecord comboRecord, LocalDate profileSaleDueDate, LocalDate profileSalePurchaseDate, String profileName, String profilePin, List<Long> accountsTypeIds, long clientId){
        log.info("ProfileSaleService:: createComboProfilesSales -> comboRecord: [{}] profileSaleDueDate: [{}] profileSalePurchaseDate: [{}] profileName: [{}] profilePin: [{}] accountsTypeIds: [{}] clientId [{}]", comboRecord, profileSaleDueDate, profileSalePurchaseDate, profileName, profilePin, accountsTypeIds, clientId);
        List<AccountTypeRecord> accountTypeRecords = this.accountTypeService.getAccountsTypeRecords(accountsTypeIds);
        List<ProfileSalesRecord> profileSalesRecords = new ArrayList<>();
        ProfileSalesRecord profileSalesRecord;
        if(accountTypeRecords.size() != accountsTypeIds.size()){
            throw new StreamTechException(i18NService.getMessage(ErrorMessages.ACCOUNTS_TYPES_LIST_NOT_FOUND));
        }
        for (AccountTypeRecord accountTypeRecord : accountTypeRecords){
            profileSalesRecord = new ProfileSalesRecord();
            profileSalesRecord.setProfileSaleName(profileName);
            profileSalesRecord.setProfileSalePin(profilePin);
            profileSalesRecord.setProfileSalePurchaseDate(profileSalePurchaseDate);
            profileSalesRecord.setProfileSaleDueDate(profileSaleDueDate);
            profileSalesRecord.setProfileSaleType(ProfileSaleType.COMBO.name());
            profileSalesRecord.setClientRecord(this.clientService.getById(clientId));
            profileSalesRecord.setAccountRecord(this.accountService.getAvailableByCombo(accountTypeRecord));
            profileSalesRecord.setProfileComboRecord(comboRecord);
            accountTypeRecord.setAccountTypeAvailableProfiles(accountTypeRecord.getAccountTypeAvailableProfiles() - 1);
            this.accountTypeService.updateAccountType(accountTypeRecord.getAccountTypeId(), accountTypeRecord);
            profileSalesRecords.add(profileSalesRecord);
        }
        log.info("ProfileSalesService:: createComboProfilesSales SaveAllProfileRecords: [{}]", profileSalesRecords);
        this.profileSalesRepository.saveAll(profileSalesRecords);
    }

    public List<ProfileSalesRecord> getSalesByAccount(long accountId){
        log.info("ProfileSalesService:: getSalesByAccount accountId: [{}]", accountId);
       return this.profileSalesRepository.getAllByAccountRecord(this.accountService.getById(accountId));
    }

    @Transactional(rollbackFor = Exception.class)
    public ProfileSalesRecord updateSaleByEmail(long profileSaleId, String email){
        log.info("ProfileSalesService:: updateSaleByEmail profileSaleId: [{}] email: [{}]", profileSaleId, email);
        AccountRecord accountRecord = this.accountService.getByEmail(email);
        ProfileSalesRecord profileSalesRecord = this.getProfileSaleById(profileSaleId);
        this.accountService.transfer(profileSalesRecord.getAccountRecord().getAccountId(), accountRecord.getAccountId());
        profileSalesRecord.setAccountRecord(accountRecord);
        this.updateProfileSale(profileSalesRecord.getProfileSaleId(), profileSalesRecord);
        log.info("ProfileSalesService:: updateSaleByEmail updated: [{}]", profileSalesRecord);
        return profileSalesRecord;
    }

    @Transactional(rollbackFor = Exception.class)
    public void saleProfilesByAccountRecord(SellProfilesByAccountRequest sellProfilesByAccountRequest){
        log.info("ProfileSalesService:: saleProfilesByAccountRecord sellProfilesByAccountRequest: [{}]", sellProfilesByAccountRequest);
        List<ProfileSalesRecord> profileSalesRecords = new ArrayList<>();
        AccountTypeRecord accountTypeRecord;
        AccountRecord accountRecord;
        ProfileSalesRecord profileSalesRecord;
        for (ProfileSaleRequest profileSaleRequest : sellProfilesByAccountRequest.profileSales) {
            accountTypeRecord = this.accountTypeService.getAccountTypeById(sellProfilesByAccountRequest.getAccountTypeId());
            accountTypeRecord.setAccountTypeAvailableProfiles(accountTypeRecord.getAccountTypeAvailableProfiles() - 1);
            accountRecord = this.accountService.getById(sellProfilesByAccountRequest.getAccountRecordId());
            profileSalesRecord = new ProfileSalesRecord();
            profileSalesRecord.setProfileSaleName(profileSaleRequest.getProfileSaleName());
            profileSalesRecord.setProfileSalePin(profileSaleRequest.getProfileSalePin());
            profileSalesRecord.setProfileSaleDueDate(profileSaleRequest.getProfileSaleDueDate());
            profileSalesRecord.setProfileSalePurchaseDate(profileSaleRequest.getProfileSaleDueDate());
            profileSalesRecord.setClientRecord(this.clientService.getById(profileSaleRequest.getClientId()));
            profileSalesRecord.setAccountRecord(accountRecord);
            profileSalesRecord.setProfileSaleType(ProfileSaleType.UNIDAD.name());
            profileSalesRecords.add(profileSalesRecord);
            this.profileSalesRepository.save(profileSalesRecord);
            this.accountTypeService.updateAccountType(accountTypeRecord.getAccountTypeId(), accountTypeRecord);
        }
    }
}
