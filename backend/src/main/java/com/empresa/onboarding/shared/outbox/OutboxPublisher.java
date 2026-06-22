package com.empresa.onboarding.shared.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OutboxPublisher {
    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);
    private final OutboxEventRepository repository;
    private final ObjectMapper objectMapper;

    public OutboxPublisher(OutboxEventRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @TransactionalEventListener
    public void handleDomainEvent(Object event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            String eventType = event.getClass().getSimpleName();
            OutboxEvent outbox = new OutboxEvent(
                    event.getClass().getPackageName(),
                    "unknown",
                    eventType,
                    payload
            );
            repository.save(outbox);
            log.debug("Event published to outbox: {}", eventType);
        } catch (Exception e) {
            log.error("Failed to publish event to outbox", e);
        }
    }
}
