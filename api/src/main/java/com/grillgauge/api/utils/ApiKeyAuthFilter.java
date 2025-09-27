package com.grillgauge.api.utils;

import com.grillgauge.api.domain.entitys.ApiKey;
import com.grillgauge.api.domain.repositorys.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Filter that checks for a valid API key in the X-API-KEY header for requests
 * to /api/v1/hub/** endpoints.
 * If a valid API key is found, the hubId is extracted and set as a request
 * attribute.
 * The filter also updates the lastUsedAt timestamp of the API key.
 */
@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private ApiKeyRepository apiKeyRepository;
    private ApiKeyGenerator apiKeyGenerator;

    public ApiKeyAuthFilter(final ApiKeyRepository apiKeyRepository, final ApiKeyGenerator apiKeyGenerator) {
        this.apiKeyRepository = apiKeyRepository;
        this.apiKeyGenerator = apiKeyGenerator;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        if (!path.startsWith("/api/v1/hub")) {
            filterChain.doFilter(request, response);
            return;
        }

        String fullApiKey = request.getHeader("X-API-KEY");
        if (fullApiKey == null || fullApiKey.isBlank()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing API key");
            return;
        }

        try {
            Long hubId = apiKeyGenerator.extractHubId(fullApiKey);
            String randomPart = apiKeyGenerator.extractRandomPart(fullApiKey);
            String hashedKey = apiKeyGenerator.hashKey(randomPart, hubId);

            Optional<ApiKey> keyOptional = apiKeyRepository.findByKeyHashAndHubIdAndActiveTrue(hashedKey, hubId);

            if (keyOptional.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid API key");
                return;
            }

            ApiKey key = keyOptional.get();
            request.setAttribute("hubId", key.getHubId());

            key.setLastUsedAt(Instant.now());
            apiKeyRepository.save(key);

            // Set SecurityContext so Spring Security knows the request is authenticated
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(key.getHubId(),
                    null, List.of());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Malformed API key");
        }
    }
}
