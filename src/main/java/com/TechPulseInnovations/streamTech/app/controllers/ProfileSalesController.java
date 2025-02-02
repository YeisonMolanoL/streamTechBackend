package com.TechPulseInnovations.streamTech.app.controllers;

import com.TechPulseInnovations.streamTech.app.modells.ProfileSalesRecord;
import com.TechPulseInnovations.streamTech.app.services.ProfileSalesService;
import com.TechPulseInnovations.streamTech.core.request.SellByProfileRequest;
import com.TechPulseInnovations.streamTech.core.request.SellProfilesByAccountRequest;
import com.TechPulseInnovations.streamTech.core.response.SaleByProfileResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.TechPulseInnovations.streamTech.core.router.Router.ProfileSalesRequestAPI.*;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping(ROOT)
@CrossOrigin(origins = "*")
@Slf4j
public class ProfileSalesController {
    private final ProfileSalesService profileSalesService;
    public ProfileSalesController(ProfileSalesService profileSalesService){
        this.profileSalesService = profileSalesService;
    }

    @PostMapping(CREATE)
    @ResponseStatus(CREATED)
    public List<SaleByProfileResponse> createAccountSales(@RequestBody List<SellByProfileRequest> profileSalesRequest){
        log.info("ProfileSalesController:: createAccountSales profileSalesRequest: {}", profileSalesRequest);
        return this.profileSalesService.createProfileSale(profileSalesRequest);
    }

    @PutMapping(UPDATE)
    public void updateAccount(@PathVariable long accountId, @RequestBody ProfileSalesRecord profileSalesRecord) throws Exception {
        this.profileSalesService.updateProfileSale(accountId, profileSalesRecord);
    }

    @GetMapping(GET_BY_ID)
    public ProfileSalesRecord getAccountSalesById(@PathVariable long accountId) throws Exception {
        return this.profileSalesService.getProfileSaleById(accountId);
    }

    @PostMapping(SOFT_DELETE)
    public void softDeleteAccountSales(@PathVariable long accountId) throws Exception {
        this.profileSalesService.softDeleteAccountSales(accountId);
    }

    @GetMapping(GET_ALL_BY_ACCOUNT)
    @ResponseStatus(OK)
    public List<ProfileSalesRecord> getSalesByAccount(@RequestParam long accountId){
        return this.profileSalesService.getSalesByAccount(accountId);
    }

    @PutMapping(UPDATE_BY_ACCOUNT_EMAIL)
    @ResponseStatus(OK)
    public ProfileSalesRecord updateProfileSaleByEmail(@RequestParam long profileSaleId, @RequestParam String email){
        return this.profileSalesService.updateSaleByEmail(profileSaleId, email);
    }

    @PostMapping(SELL_PROFILES_BY_ACCOUNT_RECORD)
    @ResponseStatus(CREATED)
    public void sellProfilesByAccountRecord(@RequestBody SellProfilesByAccountRequest sellProfilesByAccountRequest){
        this.profileSalesService.saleProfilesByAccountRecord(sellProfilesByAccountRequest);
    }
}
