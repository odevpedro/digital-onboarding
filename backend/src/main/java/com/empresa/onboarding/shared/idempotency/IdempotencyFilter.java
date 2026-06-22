package com.empresa.onboarding.shared.idempotency;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class IdempotencyFilter implements Filter {

    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
    private static final Set<String> IDEMPOTENT_METHODS = Set.of("POST", "PUT", "PATCH");
    private final IdempotencyKeyRepository repository;

    public IdempotencyFilter(IdempotencyKeyRepository repository) {
        this.repository = repository;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String idempotencyKey = httpRequest.getHeader(IDEMPOTENCY_KEY_HEADER);
        String method = httpRequest.getMethod();
        String path = httpRequest.getRequestURI();

        if (idempotencyKey == null || idempotencyKey.isBlank() || !IDEMPOTENT_METHODS.contains(method)) {
            chain.doFilter(request, response);
            return;
        }

        var cached = repository.findByIdempotencyKeyAndRequestMethodAndRequestPath(
                idempotencyKey, method, path);

        if (cached.isPresent()) {
            IdempotencyKey key = cached.get();
            if (key.getExpiresAt() != null && key.getExpiresAt().isBefore(LocalDateTime.now())) {
                repository.delete(key);
            } else {
                httpResponse.setStatus(key.getResponseStatus());
                httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
                httpResponse.getWriter().write(key.getResponseBody());
                return;
            }
        }

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(httpRequest);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(httpResponse);

        chain.doFilter(wrappedRequest, wrappedResponse);

        int status = wrappedResponse.getStatus();
        byte[] bodyBytes = wrappedResponse.getContentAsByteArray();
        String body = new String(bodyBytes, wrappedResponse.getCharacterEncoding());

        wrappedResponse.copyBodyToResponse();

        IdempotencyKey newKey = new IdempotencyKey();
        newKey.setIdempotencyKey(idempotencyKey);
        newKey.setRequestMethod(method);
        newKey.setRequestPath(path);
        newKey.setResponseStatus(status);
        newKey.setResponseBody(body);
        newKey.setCreatedAt(LocalDateTime.now());
        newKey.setExpiresAt(LocalDateTime.now().plusHours(24));
        repository.save(newKey);
    }
}
