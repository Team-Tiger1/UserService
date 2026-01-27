package com.teamtiger.userservice.users.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserRabbitMQConfig {

    public static final String EXCHANGE = "reservation.events";
    public static final String QUEUE = "reservation.user.queue";
    public static final String ROUTING_KEY = "reservation.collected";

    @Bean
    DirectExchange reservationExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    Queue userQueue() {
        return QueueBuilder.durable(QUEUE).build();
    }

    @Bean
    Binding userBinding() {
        return BindingBuilder
                .bind(userQueue())
                .to(reservationExchange())
                .with(ROUTING_KEY);
    }

    @Bean
    public JacksonJsonMessageConverter jacksonJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            JacksonJsonMessageConverter messageConverter) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        return factory;
    }



}
