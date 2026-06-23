package com.empresa.onboarding.integration.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OutboxEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OutboxEventConsumer.class);

    @RabbitListener(queues = OutboxEventConfig.QUEUE_ALL)
    public void handleEvent(String payload) {
        log.info("Consumed outbox event: {}", payload);
    }
}
