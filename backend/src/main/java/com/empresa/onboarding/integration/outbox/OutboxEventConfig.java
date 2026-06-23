package com.empresa.onboarding.integration.outbox;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OutboxEventConfig {

    public static final String EXCHANGE = "outbox.exchange";
    public static final String QUEUE_ALL = "outbox.all";
    public static final String ROUTING_PREFIX = "outbox.routing.";

    @Bean
    public TopicExchange outboxExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue outboxAllQueue() {
        return new Queue(QUEUE_ALL, true);
    }

    @Bean
    public Binding outboxAllBinding(TopicExchange outboxExchange, Queue outboxAllQueue) {
        return BindingBuilder.bind(outboxAllQueue).to(outboxExchange).with(ROUTING_PREFIX + "*");
    }
}
