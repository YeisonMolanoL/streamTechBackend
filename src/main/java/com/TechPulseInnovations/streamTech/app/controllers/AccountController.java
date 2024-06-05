package com.TechPulseInnovations.streamTech.app.controllers;

import com.TechPulseInnovations.streamTech.app.modells.AccountRecord;
import com.TechPulseInnovations.streamTech.app.modells.AccountTypeRecord;
import com.TechPulseInnovations.streamTech.app.services.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.TechPulseInnovations.streamTech.core.router.Router.AccountRequestAPI.*;
import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping(ROOT)
@Slf4j
@CrossOrigin(value = {"http://localhost:4200", "http://localhost:8080"})
public class AccountController {
    private final AccountService accountService;
    public AccountController(AccountService accountService){
        this.accountService = accountService;
    }

    @PostMapping(CREATE)
    @ResponseStatus(CREATED)
    public void createAccount(@RequestBody AccountRecord accountRecord, @RequestParam int accountTypeId){
        System.out.println(accountRecord);
        this.accountService.createAccount(accountRecord, accountTypeId);
    }

    @PutMapping(UPDATE)
    @ResponseStatus(ACCEPTED)
    public void updateAccount(@RequestParam long accountId, @RequestBody AccountRecord accountRecord) throws Exception {
        this.accountService.updateAccount(accountId, accountRecord);
    }

    @GetMapping(GET_BY_ID)
    @ResponseStatus(OK)
    public AccountRecord getById(@PathVariable long accountId) throws Exception {
        return this.accountService.getById(accountId);
    }

    @PostMapping(SOFT_DELETE)
    @ResponseStatus(CREATED)
    public void softDelete(@PathVariable long accountId) throws Exception {
        this.accountService.softDelete(accountId);
    }

    @GetMapping(GET_ALL)
    @ResponseStatus(OK)
    public List<AccountRecord> getAll(){
        return this.accountService.getAll();
    }

    @GetMapping(GET_ALL_BY_TYPE)
    @ResponseStatus(OK)
    public List<AccountRecord> getAllByType(@RequestParam int accountTypeId){
        return this.accountService.getAllByType(accountTypeId);
    }

    @PostMapping(GET_AVAILABLE)
    public Page<AccountRecord> getAvailableByAccountType(@RequestBody AccountTypeRecord accountTypeRecord, @RequestParam int page, @RequestParam int pageSize){
        return this.accountService.getAllAvailableByAccountType(accountTypeRecord,page, pageSize);
    }
}
