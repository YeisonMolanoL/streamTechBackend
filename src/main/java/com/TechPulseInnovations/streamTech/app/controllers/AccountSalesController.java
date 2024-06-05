package com.TechPulseInnovations.streamTech.app.controllers;

import com.TechPulseInnovations.streamTech.app.modells.AccountRecord;
import com.TechPulseInnovations.streamTech.app.modells.AccountSalesRecord;
import com.TechPulseInnovations.streamTech.app.services.AccountSalesService;
import org.springframework.web.bind.annotation.*;

import static com.TechPulseInnovations.streamTech.core.router.Router.AccountSalesRequestAPI.*;

@RestController
@RequestMapping(ROOT)
@CrossOrigin(value = {"http://localhost:4200", "http://localhost:8080"})
public class AccountSalesController {
    private final AccountSalesService accountSalesService;
    public AccountSalesController(AccountSalesService accountSalesService){
        this.accountSalesService = accountSalesService;
    }

    @PostMapping(CREATE)
    public void createAccountSales(@RequestBody AccountSalesRecord accountSalesRecord){
        this.accountSalesService.createAccountSales(accountSalesRecord);
    }

    @PutMapping(UPDATE)
    public void updateAccount(@PathVariable long accountId, @RequestBody AccountSalesRecord accountSalesRecord) throws Exception {
        this.accountSalesService.updateAccountSales(accountId, accountSalesRecord);
    }

    @GetMapping(GET_BY_ID)
    public AccountSalesRecord getAccountSalesById(@PathVariable long accountId) throws Exception {
        return this.accountSalesService.getAccountSalesById(accountId);
    }

    @PostMapping(SOFT_DELETE)
    public void softDeleteAccountSales(@PathVariable long accountId) throws Exception {
        this.accountSalesService.softDeleteAccountSales(accountId);
    }


}
