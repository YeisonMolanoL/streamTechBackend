package com.TechPulseInnovations.streamTech.app.controllers;

import com.TechPulseInnovations.streamTech.app.services.AccountSaleService;
import com.TechPulseInnovations.streamTech.core.request.SellByAccountRequest;
import org.springframework.web.bind.annotation.*;

import static com.TechPulseInnovations.streamTech.core.router.Router.AccountSaleRequestAPI.*;
import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping(ROOT)
@CrossOrigin(origins = "*")
public class AccountSaleController {
    public final AccountSaleService accountSaleService;

    public AccountSaleController(AccountSaleService accountSaleService) {
        this.accountSaleService = accountSaleService;
    }

    @PostMapping(CREATE)
    @ResponseStatus(CREATED)
    public void createAccountSale(@RequestBody SellByAccountRequest sellByAccountRequest){
        this.accountSaleService.createAccountSale(sellByAccountRequest);
    }
}
