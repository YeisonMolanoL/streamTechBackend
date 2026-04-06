package com.TechPulseInnovations.streamTech.app.repository;

import com.TechPulseInnovations.streamTech.codeReception.entities.Code;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CodeRepository extends JpaRepository<Code, Long> {
    
    /**
     * Obtiene el código más antiguo sin asignar para un email
     * Usa pessimistic locking para evitar condiciones de carrera
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Code c WHERE c.email = :email AND c.isAssigned = false " +
           "ORDER BY c.createdAt ASC LIMIT 1")
    Optional<Code> findFirstUnassignedByEmailWithLock(String email);
    
    /**
     * Obtiene el códogo más antiguo sin asignar (query simple sin lock)
     */
    @Query("SELECT c FROM Code c WHERE c.email = :email AND c.isAssigned = false " +
           "ORDER BY c.createdAt ASC LIMIT 1")
    Optional<Code> findFirstUnassignedByEmail(String email);
}
