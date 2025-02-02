package com.TechPulseInnovations.streamTech.app.controllers;

import com.TechPulseInnovations.streamTech.app.modells.ComboRecord;
import com.TechPulseInnovations.streamTech.app.services.ComboService;
import com.TechPulseInnovations.streamTech.core.request.ComboSaleRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.TechPulseInnovations.streamTech.core.router.Router.ComboSaleRequestAPI.*;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping(ROOT)
@CrossOrigin(origins = "*")
@Slf4j
public class ComboController {
    private final ComboService comboService;

    public ComboController(ComboService comboService){
        this.comboService = comboService;
    }

    @PostMapping(INSERT_COMBO)
    @ResponseStatus(CREATED)
    public ComboRecord insertCombo(@RequestBody ComboRecord comboRecord){
        log.info("ComboController:: insertCombo -> comboRecord: [{}]", comboRecord);
        return this.comboService.newCombo(comboRecord);
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
