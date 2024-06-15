package com.TechPulseInnovations.streamTech.app.controllers;

import com.TechPulseInnovations.streamTech.app.modells.ProfileSalesRecord;
import com.TechPulseInnovations.streamTech.app.services.ProfileSalesService;
import org.springframework.web.bind.annotation.*;

import static com.TechPulseInnovations.streamTech.core.router.Router.ProfileSalesRequestAPI.*;

@RestController
@RequestMapping(ROOT)
@CrossOrigin(value = {"http://localhost:4200", "http://localhost:8080"})
public class ProfileSalesController {
    private final ProfileSalesService profileSalesService;
    public ProfileSalesController(ProfileSalesService profileSalesService){
        this.profileSalesService = profileSalesService;
    }

    @PostMapping(CREATE)
    public void createAccountSales(@RequestBody ProfileSalesRecord profileSalesRecord){
        this.profileSalesService.createAccountSales(profileSalesRecord);
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
