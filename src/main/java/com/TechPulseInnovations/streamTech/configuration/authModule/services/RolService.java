package com.TechPulseInnovations.streamTech.configuration.authModule.services;

import com.TechPulseInnovations.streamTech.app.services.I18NService;
import com.TechPulseInnovations.streamTech.configuration.authModule.models.RolRecord;
import com.TechPulseInnovations.streamTech.configuration.authModule.repository.RolRepository;
import com.TechPulseInnovations.streamTech.core.errorException.ErrorMessages;
import com.TechPulseInnovations.streamTech.core.errorException.StreamTechException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RolService {
    private final RolRepository rolRepository;
    private final I18NService i18NService;

    public RolService(RolRepository rolRepository, I18NService i18NService) {
        this.rolRepository = rolRepository;
        this.i18NService = i18NService;
    }

    public RolRecord getById(long rolId) {
        return this.rolRepository.findById(rolId).orElseThrow(() -> new StreamTechException(i18NService.getMessage(ErrorMessages.ROLE_NOT_FOUND, rolId)));
    }
}
