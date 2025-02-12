package com.TechPulseInnovations.streamTech.configuration.authModule.services;

import com.TechPulseInnovations.streamTech.app.services.I18NService;
import com.TechPulseInnovations.streamTech.configuration.authModule.core.dto.JwtDto;
import com.TechPulseInnovations.streamTech.configuration.authModule.core.jwt.JwtProvider;
import com.TechPulseInnovations.streamTech.configuration.authModule.core.request.NewUserRequest;
import com.TechPulseInnovations.streamTech.configuration.authModule.models.RolRecord;
import com.TechPulseInnovations.streamTech.configuration.authModule.models.UserRecord;
import com.TechPulseInnovations.streamTech.configuration.authModule.repository.UserRepository;
import com.TechPulseInnovations.streamTech.core.errorException.ErrorMessages;
import com.TechPulseInnovations.streamTech.core.errorException.StreamTechException;
import com.TechPulseInnovations.streamTech.core.request.LogInRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public class UserService {
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RolService rolService;
    private final I18NService i18NService;
    public UserService(RolService rolService, UserRepository userRepository, I18NService i18NService, PasswordEncoder passwordEncoder){
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.i18NService = i18NService;
        this.rolService = rolService;
    }

    @Transactional(rollbackFor = Exception.class)
    public JwtDto createUser(NewUserRequest newUserRequest){
        String passwordEncode = passwordEncoder.encode(newUserRequest.getPassword());
        try {
        log.info("UserService:: createUser newUserRequest: [{}]", newUserRequest);
        UserRecord userRecord = new UserRecord();
        userRecord.setUserName(newUserRequest.getUserName());
        userRecord.setName(newUserRequest.getName());
        userRecord.setLastName(newUserRequest.getLastName());
        userRecord.setPassword(passwordEncode);
        RolRecord rolRecord = rolService.getById(1);
        userRecord.setRoles(Set.of(rolRecord));
        userRecord = this.userRepository.save(userRecord);
        Authentication authentication =
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(newUserRequest.getUserName(), newUserRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtProvider.generateToken(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        JwtDto jwtDto = new JwtDto(jwt, userDetails.getUsername(), userDetails.getAuthorities());
        return jwtDto;
        } catch (Exception e) {
            log.error("Error autenticando usuario: ", e);
            throw new RuntimeException("Error en autenticación");
        }
    }

    public JwtDto logIn(LogInRequest logInRequest){
        log.info("UserService:: logIn logInRequest: [{}]", logInRequest);
        UserRecord userRecord = this.userRepository.findByUserName(logInRequest.getUserName()).orElseThrow(() -> new StreamTechException(i18NService.getMessage(ErrorMessages.USER_NOT_FOUND)));
        if (!passwordEncoder.matches(logInRequest.getPassword(), userRecord.getPassword())) {
            throw new StreamTechException(i18NService.getMessage(ErrorMessages.USER_DATA_NOT_FOUND));
        }
        Authentication authentication =
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(logInRequest.getUserName(), logInRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtProvider.generateToken(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        JwtDto jwtDto = new JwtDto(jwt, userDetails.getUsername(), userDetails.getAuthorities());
        return jwtDto;
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
