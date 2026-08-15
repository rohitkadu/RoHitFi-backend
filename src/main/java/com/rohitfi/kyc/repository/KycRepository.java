package com.rohitfi.kyc.repository;

import com.rohitfi.kyc.document.KycDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KycRepository extends MongoRepository<KycDocument, String> {
    
    // Notice the exact return type: Optional<KycDocument>
    Optional<KycDocument> findByCustomerId(Long customerId);
    
    boolean existsByCustomerId(Long customerId);
    
    // Notice the exact return type: List<KycDocument>
    List<KycDocument> findByStatus(KycDocument.KycStatus status);
    
    long countByStatus(com.rohitfi.kyc.document.KycDocument.KycStatus status);
}