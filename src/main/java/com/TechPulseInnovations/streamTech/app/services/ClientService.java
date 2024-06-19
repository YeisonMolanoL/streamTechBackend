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
    public ClientService(ClientRepository clientRepository){
        this.clientRepository = clientRepository;
    }

    public List<ClientRecord> getAll(){
        return this.clientRepository.findAll();
    }

    public ClientRecord getById(long clientId){
        log.info("ClientService:: getById -> clientId: [{}]", clientId);
        return this.clientRepository.findById(clientId).orElseThrow(() -> new StreamTechException(ErrorMessages.CLIENT_NOT_FOUND));
    }
}
