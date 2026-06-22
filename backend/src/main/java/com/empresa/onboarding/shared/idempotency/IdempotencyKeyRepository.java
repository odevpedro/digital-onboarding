package com.empresa.onboarding.shared.idempotency;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, String> {
    Optional<IdempotencyKey> findByIdempotencyKeyAndRequestMethodAndRequestPath(
            String idempotencyKey, String requestMethod, String requestPath);
}
