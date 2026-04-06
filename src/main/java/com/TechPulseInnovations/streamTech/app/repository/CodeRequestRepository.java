package com.TechPulseInnovations.streamTech.app.repository;

import com.TechPulseInnovations.streamTech.codeReception.entities.CodeRequest;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CodeRequestRepository extends JpaRepository<CodeRequest, Long> {
    
    /**
     * Obtiene la solicitud más antigua pendiente para un email
     * Usa pessimistic locking para garantizar FIFO
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT cr FROM CodeRequest cr WHERE cr.email = :email " +
           "AND cr.status = 'PENDING' ORDER BY cr.createdAt ASC LIMIT 1")
    Optional<CodeRequest> findFirstPendingByEmailWithLock(String email);
    
    /**
     * Obtiene solicitud sin lock (para consultas de lectura)
     */
    @Query("SELECT cr FROM CodeRequest cr WHERE cr.email = :email " +
           "AND cr.status = 'PENDING' ORDER BY cr.createdAt ASC LIMIT 1")
    Optional<CodeRequest> findFirstPendingByEmail(String email);
    
    Optional<CodeRequest> findByEmailAndStatus(String email, CodeRequest.CodeRequestStatus status);
}
