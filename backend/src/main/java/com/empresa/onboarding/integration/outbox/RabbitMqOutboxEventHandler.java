package com.empresa.onboarding.integration.outbox;

import br.com.odevpedro.foundation.web.outbox.OutboxEvent;
import br.com.odevpedro.foundation.web.outbox.OutboxEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitMqOutboxEventHandler implements OutboxEventHandler {

    private static final Logger log = LoggerFactory.getLogger(RabbitMqOutboxEventHandler.class);

    private final RabbitTemplate rabbitTemplate;

    public RabbitMqOutboxEventHandler(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void handle(OutboxEvent event) {
        var routingKey = OutboxEventConfig.ROUTING_PREFIX + event.getEventType();
        log.info("Publishing outbox event {} to {} with key {}",
                event.getId(), OutboxEventConfig.EXCHANGE, routingKey);
        rabbitTemplate.convertAndSend(OutboxEventConfig.EXCHANGE, routingKey, event.getPayload());
    }
}
