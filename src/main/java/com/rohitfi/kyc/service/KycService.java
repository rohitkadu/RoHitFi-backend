package com.rohitfi.kyc.service;

import com.rohitfi.auth.entity.User;
import com.rohitfi.auth.repository.UserRepository;
import com.rohitfi.common.exception.ResourceNotFoundException;
import com.rohitfi.customer.entity.Customer;
import com.rohitfi.customer.repository.CustomerRepository;
import com.rohitfi.kyc.document.KycDocument;
import com.rohitfi.kyc.dto.KycResponse;
import com.rohitfi.kyc.dto.KycSubmitRequest;
import com.rohitfi.kyc.repository.KycRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class KycService {

    private final KycRepository kycRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    public KycResponse submitKyc(String mobile, KycSubmitRequest request) {
        User user = userRepository.findByMobile(mobile)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Customer customer = customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Complete profile before submitting KYC"));

        if (kycRepository.existsByCustomerId(customer.getId())) {
            throw new RuntimeException("KYC request already exists for this customer");
        }

        // Using placeholders to adhere to data security protocols regarding sensitive IDs
        String maskedNo = request.getDocumentType().equalsIgnoreCase("AADHAAR") 
                ? "XXXX-XXXX-" + customer.getAadhaarLast4() 
                : customer.getPan();

        KycDocument kyc = KycDocument.builder()
                .customerId(customer.getId())
                .pan(customer.getPan())
                .documentType(request.getDocumentType())
                .maskedDocumentNo(maskedNo)
                .documentUrl(request.getDocumentUrl())
                .status(KycDocument.KycStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        KycDocument saved = kycRepository.save(kyc);
        return mapToResponse(saved);
    }

    public KycResponse getMyKycStatus(String mobile) {
        User user = userRepository.findByMobile(mobile)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Customer customer = customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"));

        KycDocument kyc = kycRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("KYC not submitted yet"));

        return mapToResponse(kyc);
    }

    public KycResponse verifyKyc(String kycId, boolean approved, String remark, Long managerId) {
        KycDocument kyc = kycRepository.findById(kycId)
                .orElseThrow(() -> new ResourceNotFoundException("KYC record not found"));

        kyc.setStatus(approved ? KycDocument.KycStatus.VERIFIED : KycDocument.KycStatus.REJECTED);
        kyc.setManagerRemark(remark);
        kyc.setVerifiedByManagerId(managerId);
        kyc.setUpdatedAt(LocalDateTime.now());

        KycDocument saved = kycRepository.save(kyc);
        return mapToResponse(saved);
    }

    private KycResponse mapToResponse(KycDocument doc) {
        return KycResponse.builder()
                .id(doc.getId())
                .customerId(doc.getCustomerId())
                .documentType(doc.getDocumentType())
                .maskedDocumentNo(doc.getMaskedDocumentNo())
                .documentUrl(doc.getDocumentUrl())
                .status(doc.getStatus())
                .managerRemark(doc.getManagerRemark())
                .createdAt(doc.getCreatedAt())
                .build();
    }
}