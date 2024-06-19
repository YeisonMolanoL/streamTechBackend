package com.TechPulseInnovations.streamTech.app.controllers;

import com.TechPulseInnovations.streamTech.app.modells.ProfileSalesRecord;
import com.TechPulseInnovations.streamTech.app.services.ProfileSalesService;
import com.TechPulseInnovations.streamTech.core.request.SellByProfileRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.TechPulseInnovations.streamTech.core.router.Router.ProfileSalesRequestAPI.*;
import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping(ROOT)
@CrossOrigin(value = {"http://localhost:4200", "http://localhost:8080"})
@Slf4j
public class ProfileSalesController {
    private final ProfileSalesService profileSalesService;
    public ProfileSalesController(ProfileSalesService profileSalesService){
        this.profileSalesService = profileSalesService;
    }

    @PostMapping(CREATE)
    @ResponseStatus(CREATED)
    public void createAccountSales(@RequestBody List<SellByProfileRequest> profileSalesRequest){
        log.info("ProfileSalesController:: createAccountSales profileSalesRequest: {}", profileSalesRequest);
        this.profileSalesService.createAccountSales(profileSalesRequest);
    }

    @PutMapping(UPDATE)
    public void updateAccount(@PathVariable long accountId, @RequestBody ProfileSalesRecord profileSalesRecord) throws Exception {
        this.profileSalesService.updateAccountSales(accountId, profileSalesRecord);
    }

    @GetMapping(GET_BY_ID)
    public ProfileSalesRecord getAccountSalesById(@PathVariable long accountId) throws Exception {
        return this.profileSalesService.getAccountSalesById(accountId);
    }

    @PostMapping(SOFT_DELETE)
    public void softDeleteAccountSales(@PathVariable long accountId) throws Exception {
        this.profileSalesService.softDeleteAccountSales(accountId);
    }


}
