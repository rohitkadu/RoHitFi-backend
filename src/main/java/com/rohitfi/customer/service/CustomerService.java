package com.rohitfi.customer.service;

import com.rohitfi.auth.entity.User;
import com.rohitfi.auth.repository.UserRepository;
import com.rohitfi.common.exception.ResourceNotFoundException;
import com.rohitfi.customer.dto.CustomerProfileRequest;
import com.rohitfi.customer.dto.CustomerProfileResponse;
import com.rohitfi.customer.entity.Customer;
import com.rohitfi.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    @Transactional
    public CustomerProfileResponse createProfile(String mobile, CustomerProfileRequest request) {
        User user = userRepository.findByMobile(mobile)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (customerRepository.existsByUserId(user.getId())) {
            throw new RuntimeException("Profile already exists for this user");
        }

        if (customerRepository.existsByPan(request.getPan())) {
            throw new RuntimeException("PAN is already registered with another account");
        }

        Customer customer = Customer.builder()
                .userId(user.getId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dob(request.getDob())
                .gender(request.getGender())
                .pan(request.getPan())
                .aadhaarLast4(request.getAadhaarLast4())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .build();

        Customer saved = customerRepository.save(customer);
        return mapToResponse(saved);
    }

    public CustomerProfileResponse getMyProfile(String mobile) {
        User user = userRepository.findByMobile(mobile)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Customer customer = customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found. Please complete registration."));

        return mapToResponse(customer);
    }

    private CustomerProfileResponse mapToResponse(Customer customer) {
        return CustomerProfileResponse.builder()
                .id(customer.getId())
                .fullName(customer.getFirstName() + " " + customer.getLastName())
                .dob(customer.getDob())
                .gender(customer.getGender())
                .pan(customer.getPan())
                .aadhaarLast4(customer.getAadhaarLast4())
                .fullAddress(customer.getAddress() + ", " + customer.getCity() + ", " + customer.getState() + " - " + customer.getPincode())
                .build();
    }
}