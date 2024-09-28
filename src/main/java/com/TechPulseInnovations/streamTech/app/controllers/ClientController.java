package com.TechPulseInnovations.streamTech.app.controllers;

import com.TechPulseInnovations.streamTech.app.modells.ClientRecord;
import com.TechPulseInnovations.streamTech.app.services.ClientService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.TechPulseInnovations.streamTech.core.router.Router.ClientRequestAPI.*;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping(ROOT)
@CrossOrigin(value = {"http://localhost:4200", "http://localhost:8080"})
public class ClientController {
    private final ClientService clientService;

    public ClientController(ClientService clientService){
        this.clientService = clientService;
    }

    @GetMapping(GET_ALL)
    @ResponseStatus(OK)
    public List<ClientRecord> getAll(){
        return this.clientService.getAll();
    }

    @PostMapping(CREATE)
    @ResponseStatus(CREATED)
    public void newClient(@RequestBody ClientRecord clientRecord){
        this.clientService.newClient(clientRecord);
    }
}
