package com.rohitfi.common.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final IdempotencyKeyRepository idempotencyRepository;
    private final ObjectMapper objectMapper;

    public Optional getExistingKey(String key) {
        if (key == null || key.isBlank()) {
            return Optional.empty();
        }
        return idempotencyRepository.findByKey(key);
    }

    @Transactional
    public void recordResponse(String key, Long userId, String requestPath, Object responseBody, int status) {
        if (key == null || key.isBlank()) {
            return;
        }
        try {
            String serialized = objectMapper.writeValueAsString(responseBody);
            IdempotencyKey record = IdempotencyKey.builder()
                    .key(key)
                    .userId(userId)
                    .requestPath(requestPath)
                    .responseBody(serialized)
                    .responseStatus(status)
                    .build();
            idempotencyRepository.save(record);
        } catch (Exception ignored) {
            // If recording fails, prevent blocking the core transaction
        }
    }
}