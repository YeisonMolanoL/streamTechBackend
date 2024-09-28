package com.TechPulseInnovations.streamTech.app.services;

import com.TechPulseInnovations.streamTech.app.modells.ComboRecord;
import com.TechPulseInnovations.streamTech.app.repository.ComboRepository;
import com.TechPulseInnovations.streamTech.core.errorException.ErrorMessages;
import com.TechPulseInnovations.streamTech.core.errorException.StreamTechException;
import com.TechPulseInnovations.streamTech.core.request.ComboSaleRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class ComboService {
    private final ProfileSalesService profileSalesService;
    private final ComboRepository comboRepository;
    private final I18NService i18NService;

    public ComboService(I18NService i18NService, ProfileSalesService profileSalesService, ComboRepository comboRepository){
        this.profileSalesService = profileSalesService;
        this.comboRepository = comboRepository;
        this.i18NService = i18NService;
    }

    @Transactional(rollbackFor = Exception.class)
    public ComboRecord newCombo(ComboRecord comboRecord){
        log.info("ComboService:: newCombo -> comboRecord: [{}]", comboRecord);
        return this.comboRepository.save(comboRecord);
    }

    @Transactional(rollbackFor = Exception.class)
    public void newComboSale(ComboSaleRequest comboSaleRequest){
        log.info("ComboService:: newComboSale -> comboSaleRequest: [{}]", comboSaleRequest);
        ComboRecord comboTemporal = new ComboRecord();
        ComboRecord comboRecord;
        if(comboSaleRequest.getExistingCombo() == 0){
            comboTemporal.setComboName(comboSaleRequest.getComboName());
            comboRecord = this.newCombo(comboTemporal);
        }else{
            comboRecord = this.comboRepository.findById(comboSaleRequest.getExistingCombo()).orElseThrow(() -> new StreamTechException(i18NService.getMessage(ErrorMessages.COMBO_NOT_FOUND)));
        }
        this.profileSalesService.createComboProfilesSales(comboRecord, comboSaleRequest.getProfileSaleDueDate(), comboSaleRequest.getProfileSalePurchaseDate(), comboSaleRequest.getProfileSaleName(), comboSaleRequest.getProfileSalePin(), comboSaleRequest.getComboAccountsType(), comboSaleRequest.getClientId());
    }

    public List<ComboRecord> getAll(){
        log.info("ComboService:: getAll()");
        return this.comboRepository.findAll();
    }
}
