package com.TechPulseInnovations.streamTech.app.controllers;

import com.TechPulseInnovations.streamTech.app.modells.AccountTypeRecord;
import com.TechPulseInnovations.streamTech.app.services.AccountTypeService;
import com.TechPulseInnovations.streamTech.core.response.AccountTypeTotalResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.TechPulseInnovations.streamTech.core.router.Router.AccountTypeRequestAPI.*;

@RestController
@RequestMapping(ROOT)
@CrossOrigin(value = {"http://localhost:4200", "http://localhost:8080"})
public class AccountTypeController {
    private final AccountTypeService accountTypeService;

    public AccountTypeController(AccountTypeService accountTypeService) {
        this.accountTypeService = accountTypeService;
    }

    @PostMapping(CREATE)
    public void createAccountType(@RequestBody AccountTypeRecord accountTypeRecord){
        this.accountTypeService.createAccountType(accountTypeRecord);
    }

    @PutMapping(UPDATE)
    public void updateAccountType(@PathVariable long accountId, @RequestBody AccountTypeRecord accountTypeRecord) throws Exception {
        this.accountTypeService.updateAccountType(accountId, accountTypeRecord);
    }

    @GetMapping(GET_BY_ID)
    public AccountTypeRecord getAccountTypeById(@PathVariable long accountId) throws Exception {
        return this.accountTypeService.getAccountTypeById(accountId);
    }

    @PostMapping(SOFT_DELETE)
    public void softDeleteAccountType(@PathVariable long accountId) throws Exception {
        this.accountTypeService.softDeleteAccountType(accountId);
    }

    @GetMapping(GET_ALL)
    public List<AccountTypeRecord> getAll(){
        return this.accountTypeService.getAll();
    }

    @GetMapping(GET_ALL_DATA)
    public List<AccountTypeTotalResponse> getAllTotalData(){
        return this.accountTypeService.getAccountTotalInformation();
    }
}
