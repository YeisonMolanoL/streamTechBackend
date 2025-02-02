package com.TechPulseInnovations.streamTech.configuration.authModule.services;

import com.TechPulseInnovations.streamTech.app.services.I18NService;
import com.TechPulseInnovations.streamTech.configuration.authModule.models.UserRecord;
import com.TechPulseInnovations.streamTech.configuration.authModule.repository.UserRepository;
import com.TechPulseInnovations.streamTech.core.errorException.ErrorMessages;
import com.TechPulseInnovations.streamTech.core.errorException.StreamTechException;
import com.TechPulseInnovations.streamTech.core.request.LogInRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final I18NService i18NService;
    public UserService(UserRepository userRepository, I18NService i18NService){
        this.userRepository = userRepository;
        this.i18NService = i18NService;
    }

    @Transactional(rollbackFor = Exception.class)
    public UserRecord createUser(UserRecord userRecord){
        log.info("UserService:: createUser userRecord: [{}]", userRecord);
        userRecord = this.userRepository.save(userRecord);
        return userRecord;
    }

    public UserRecord logIn(LogInRequest logInRequest){
        log.info("UserService:: logIn logInRequest: [{}]", logInRequest);
        if(!this.userRepository.existsByUserName(logInRequest.getUserName())){
            throw new StreamTechException(i18NService.getMessage(ErrorMessages.USER_NOT_FOUND));
        }
        UserRecord userRecord = this.userRepository.findByUserNameAndPassword(logInRequest.getUserName(), logInRequest.getPassword()).orElseThrow(() -> new StreamTechException(i18NService.getMessage(ErrorMessages.USER_DATA_NOT_FOUND)));
        return userRecord;
    }

    public void updateUser(long userId, UserRecord userRecord) throws Exception {
        Optional<UserRecord> userRecordFound = this.userRepository.findById(userId);
        if(userRecordFound.isEmpty()){
            throw new Exception("Error al encontrar la cuenta con id: {"+ userId + "}");
        }
        userRecordFound.get().setUserName(userRecord.getUserName());
        userRecordFound.get().setEnabled(userRecord.isEnabled());
        userRecordFound.get().setName(userRecord.getName());
        userRecordFound.get().setPassword(userRecord.getPassword());
        userRecordFound.get().setLastName(userRecord.getLastName());
        this.userRepository.save(userRecordFound.get());
    }

    public UserRecord getById(long userId) {
        return this.userRepository.findById(userId).orElseThrow(() -> new StreamTechException(i18NService.getMessage(ErrorMessages.ACCOUNT_NOT_FOUND)));
    }

    public void softDelete(long accountId) throws Exception {
        UserRecord userRecord = this.userRepository.findById(accountId).orElseThrow(() -> new Exception("Error al encontrar la cuenta con id: {"+ accountId + "}"));
        userRecord.setEnabled(!userRecord.isEnabled());
        this.userRepository.save(userRecord);
    }

    public List<UserRecord> getAll(){
        return this.userRepository.findAll();
    }
}
