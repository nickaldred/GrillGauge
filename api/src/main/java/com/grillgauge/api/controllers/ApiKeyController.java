package com.grillgauge.api.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.grillgauge.api.domain.entitys.ApiKey;
import com.grillgauge.api.domain.repositorys.ApiKeyRepository;
import com.grillgauge.api.utils.ApiKeyGenerator;

@RestController
@RequestMapping("/api/v1/api-key")
public class ApiKeyController {

    private final ApiKeyGenerator apiKeyGenerator;
    private final ApiKeyRepository apiKeyRepository;

    public ApiKeyController(final ApiKeyGenerator apiKeyGenerator, final ApiKeyRepository apiKeyRepository) {
        this.apiKeyGenerator = apiKeyGenerator;
        this.apiKeyRepository = apiKeyRepository;

    }

    @PostMapping("/generate")
    public String generateApiKey(@RequestParam Long hubId, @RequestParam(required = false) String label) {
        String randomKey = apiKeyGenerator.generateRandomKey();
        String fullKey = apiKeyGenerator.buildFullApiKey(randomKey, hubId);
        String hashedKey = apiKeyGenerator.hashKey(randomKey, hubId);

        ApiKey apiKey = new ApiKey(hashedKey, hubId, label);
        apiKeyRepository.save(apiKey);

        return fullKey;
    }
}
