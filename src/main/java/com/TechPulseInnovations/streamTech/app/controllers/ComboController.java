package com.TechPulseInnovations.streamTech.app.controllers;

import com.TechPulseInnovations.streamTech.app.modells.ComboRecord;
import com.TechPulseInnovations.streamTech.app.services.ComboService;
import com.TechPulseInnovations.streamTech.core.request.ComboSaleRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.TechPulseInnovations.streamTech.core.router.Router.ComboSaleRequestAPI.*;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping(ROOT)
@CrossOrigin(value = {"http://localhost:4200", "http://localhost:8080"})
public class ComboController {
    private final ComboService comboService;

    public ComboController(ComboService comboService){
        this.comboService = comboService;
    }

    @PostMapping(CREATE)
    @ResponseStatus(CREATED)
    public void newComboSale(@RequestBody ComboSaleRequest comboSaleRequest){
        this.comboService.newComboSale(comboSaleRequest);
    }

    @GetMapping(GET_ALL)
    @ResponseStatus(OK)
    public List<ComboRecord> getAll(){
        return this.comboService.getAll();
    }
}
