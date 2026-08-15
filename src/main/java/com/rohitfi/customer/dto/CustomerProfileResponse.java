package com.rohitfi.customer.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class CustomerProfileResponse {
    private Long id;
    private String fullName;
    private LocalDate dob;
    private String gender;
    private String pan;
    private String aadhaarLast4;
    private String fullAddress;
}