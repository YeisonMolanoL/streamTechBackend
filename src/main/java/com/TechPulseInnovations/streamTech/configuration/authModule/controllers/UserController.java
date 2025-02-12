package com.TechPulseInnovations.streamTech.configuration.authModule.controllers;

import com.TechPulseInnovations.streamTech.app.modells.AccountRecord;
import com.TechPulseInnovations.streamTech.app.services.AccountService;
import com.TechPulseInnovations.streamTech.configuration.authModule.core.dto.JwtDto;
import com.TechPulseInnovations.streamTech.configuration.authModule.core.request.NewUserRequest;
import com.TechPulseInnovations.streamTech.configuration.authModule.models.UserRecord;
import com.TechPulseInnovations.streamTech.configuration.authModule.services.UserService;
import com.TechPulseInnovations.streamTech.core.request.LogInRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import static com.TechPulseInnovations.streamTech.core.router.Router.UsersRequestAPI.*;
import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping(ROOT)
@Slf4j
@CrossOrigin(origins = "*")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService){
        this.userService = userService;
    }

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping(CREATE)
    @ResponseStatus(CREATED)
    public JwtDto createAccount(@RequestBody NewUserRequest newUserRequest){
        return this.userService.createUser(newUserRequest);
    }

    @PostMapping(LOGIN)
    @ResponseStatus(CREATED)
    public JwtDto logIn(@RequestBody LogInRequest accountRecord){
        System.out.println("soy " + accountRecord);
        return this.userService.logIn(accountRecord);
    }

    @PutMapping(UPDATE)
    @ResponseStatus(ACCEPTED)
    public void updateAccount(@RequestParam long accountId, @RequestBody UserRecord userRecord) throws Exception {
        this.userService.updateUser(accountId, userRecord);
    }

    @GetMapping(GET_BY_ID)
    @ResponseStatus(OK)
    public UserRecord getById(@PathVariable long accountId) throws Exception {
        return this.userService.getById(accountId);
    }

    @PostMapping(SOFT_DELETE)
    @ResponseStatus(CREATED)
    public void softDelete(@PathVariable long accountId) throws Exception {
        this.userService.softDelete(accountId);
    }

    @GetMapping(GET_ALL)
    @ResponseStatus(OK)
    public List<UserRecord> getAll(){
        return this.userService.getAll();
    }
}
