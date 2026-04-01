package com.TechPulseInnovations.streamTech.configuration.authModule.services;

import com.TechPulseInnovations.streamTech.configuration.authModule.models.RolRecord;
import com.TechPulseInnovations.streamTech.configuration.authModule.models.UserRecord;
import com.TechPulseInnovations.streamTech.configuration.authModule.models.PrincipalUser;
import com.TechPulseInnovations.streamTech.configuration.authModule.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserDetailService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserRecord> userRecord = userRepository.findByUserName(username);

        if (userRecord.isPresent()) {
            var authorities = userRecord.get().getRoles().stream()
                    .map(rol -> new SimpleGrantedAuthority(rol.getName()))
                    .collect(Collectors.toList());

            return new PrincipalUser(
                    userRecord.get().getName(),
                    userRecord.get().getUserName(),
                    userRecord.get().getPassword(),
                    authorities
            );
        } else {
            throw new UsernameNotFoundException(username);
        }
    }
}
