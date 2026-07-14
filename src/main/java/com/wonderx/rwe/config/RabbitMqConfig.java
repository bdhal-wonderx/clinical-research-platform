package com.wonderx.rwe.config;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnBean(ConnectionFactory.class)
public class RabbitMqConfig {

    public static final String PLATFORM_EXCHANGE = "rwe.platform.exchange";
    public static final String OCR_QUEUE = "rwe.ocr.queue";
    public static final String VALIDATION_QUEUE = "rwe.validation.queue";
    public static final String DASHBOARD_QUEUE = "rwe.dashboard.queue";

    public static final String OCR_ROUTING_KEY = "ocr.request";
    public static final String VALIDATION_ROUTING_KEY = "validation.request";
    public static final String DASHBOARD_ROUTING_KEY = "dashboard.refresh";

    @Bean
    public org.springframework.amqp.core.TopicExchange platformExchange() {
        return new org.springframework.amqp.core.TopicExchange(PLATFORM_EXCHANGE);
    }

    @Bean
    public org.springframework.amqp.core.Queue ocrQueue() {
        return org.springframework.amqp.core.QueueBuilder.durable(OCR_QUEUE).build();
    }

    @Bean
    public org.springframework.amqp.core.Queue validationQueue() {
        return org.springframework.amqp.core.QueueBuilder.durable(VALIDATION_QUEUE).build();
    }

    @Bean
    public org.springframework.amqp.core.Binding ocrBinding(
            org.springframework.amqp.core.Queue ocrQueue,
            org.springframework.amqp.core.TopicExchange platformExchange) {
        return org.springframework.amqp.core.BindingBuilder.bind(ocrQueue)
                .to(platformExchange).with(OCR_ROUTING_KEY);
    }

    @Bean
    public org.springframework.amqp.core.Binding validationBinding(
            org.springframework.amqp.core.Queue validationQueue,
            org.springframework.amqp.core.TopicExchange platformExchange) {
        return org.springframework.amqp.core.BindingBuilder.bind(validationQueue)
                .to(platformExchange).with(VALIDATION_ROUTING_KEY);
    }
}
