package com.empresa.onboarding.shared.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class OutboxProcessor {
    private static final Logger log = LoggerFactory.getLogger(OutboxProcessor.class);
    private final OutboxEventRepository repository;

    public OutboxProcessor(OutboxEventRepository repository) {
        this.repository = repository;
    }

    @Scheduled(fixedDelayString = "${onboarding.outbox.poll-interval-ms:5000}")
    @Transactional
    public void processPendingEvents() {
        var pending = repository.findByStatusOrderByCreatedAtAsc("PENDING");
        for (OutboxEvent event : pending) {
            try {
                log.info("Processing outbox event: {} [{}]", event.getEventType(), event.getId());
                event.setStatus("PROCESSED");
                event.setProcessedAt(LocalDateTime.now());
                repository.save(event);
            } catch (Exception e) {
                log.error("Failed to process outbox event: {}", event.getId(), e);
                event.setStatus("FAILED");
                repository.save(event);
            }
        }
    }
}
