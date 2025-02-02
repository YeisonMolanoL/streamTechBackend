package com.TechPulseInnovations.streamTech.app.controllers;

import com.TechPulseInnovations.streamTech.app.modells.AccountTypeRecord;
import com.TechPulseInnovations.streamTech.app.services.AccountTypeService;
import com.TechPulseInnovations.streamTech.core.response.AccountTypeTotalResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.TechPulseInnovations.streamTech.core.router.Router.AccountTypeRequestAPI.*;
import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping(ROOT)
@CrossOrigin(origins = "*")
public class AccountTypeController {
    private final AccountTypeService accountTypeService;

    public AccountTypeController(AccountTypeService accountTypeService) {
        this.accountTypeService = accountTypeService;
    }

    @PostMapping(CREATE)
    @ResponseStatus(CREATED)
    public void createAccountType(@RequestBody AccountTypeRecord accountTypeRecord){
        this.accountTypeService.createAccountType(accountTypeRecord);
    }

    @PutMapping(UPDATE)
    @ResponseStatus(ACCEPTED)
    public void updateAccountType(@PathVariable long accountId, @RequestBody AccountTypeRecord accountTypeRecord) throws Exception {
        this.accountTypeService.updateAccountType(accountId, accountTypeRecord);
    }

    @GetMapping(GET_BY_ID)
    @ResponseStatus(OK)
    public AccountTypeRecord getAccountTypeById(@PathVariable long accountId) throws Exception {
        return this.accountTypeService.getAccountTypeById(accountId);
    }

    @PostMapping(SOFT_DELETE)
    @ResponseStatus(CONTINUE)
    public void softDeleteAccountType(@PathVariable long accountId) throws Exception {
        this.accountTypeService.softDeleteAccountType(accountId);
    }

    @GetMapping(GET_ALL)
    @ResponseStatus(OK)
    public List<AccountTypeRecord> getAll(){
        return this.accountTypeService.getAll();
    }

    @GetMapping(GET_ALL_DATA)
    @ResponseStatus(OK)
    public List<AccountTypeTotalResponse> getAllTotalData(){
        return this.accountTypeService.getAccountTotalInformation();
    }

    @GetMapping(GET_ALL_AVAILABLE_PROFILE)
    @ResponseStatus(OK)
    public List<AccountTypeRecord> getAllByAvailableProfile(){
        return this.accountTypeService.getAllWithAvailableProfile();
    }
}
