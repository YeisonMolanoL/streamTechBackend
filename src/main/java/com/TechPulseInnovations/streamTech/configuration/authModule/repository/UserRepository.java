package com.TechPulseInnovations.streamTech.configuration.authModule.repository;

import com.TechPulseInnovations.streamTech.configuration.authModule.models.UserRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserRecord, Long> {
    boolean existsByUserName(String userName);
    Optional<UserRecord> findByUserNameAndPassword(String userName, String password);
    Optional<UserRecord> findByUserName(String userName);
}
