package com.TechPulseInnovations.streamTech.app.services;

import com.TechPulseInnovations.streamTech.app.modells.ClientRecord;
import com.TechPulseInnovations.streamTech.app.repository.ClientRepository;
import com.TechPulseInnovations.streamTech.core.errorException.ErrorMessages;
import com.TechPulseInnovations.streamTech.core.errorException.StreamTechException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ClientService {
    private final ClientRepository clientRepository;
    private final I18NService i18NService;
    public ClientService(I18NService i18NService, ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
        this.i18NService = i18NService;
    }

    public List<ClientRecord> getAll(){
        log.info("ClientService:: getAll");
        return this.clientRepository.findAll();
    }

    public ClientRecord getById(long clientId){
        log.info("ClientService:: getById -> clientId: [{}]", clientId);
        return this.clientRepository.findById(clientId).orElseThrow(() -> new StreamTechException(i18NService.getMessage(ErrorMessages.CLIENT_NOT_FOUND)));
    }

    public void newClient(ClientRecord clientRecord){
        log.info("ClientService:: newClient -> clientRecord: [{}]", clientRecord);
        this.clientRepository.save(clientRecord);
    }
}
