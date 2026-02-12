package com.teamtiger.userservice.users.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Configuration for the RabbitMQ Listener
 * Sets up the queue and bindings
 */
@Configuration
public class UserRabbitMQConfig {

    public static final String EXCHANGE = "reservation.events";
    public static final String QUEUE = "reservation.user.queue";
    public static final String ROUTING_KEY = "reservation.collected";

    /**
     * Defines a Direct Exchange for events
     */
    @Bean
    DirectExchange reservationExchange() {
        return new DirectExchange(EXCHANGE);
    }

    /**
     * Defines a queue to store messages
     * Durable - Messages will persist after a RabbitMQ restart
     */
    @Bean
    Queue userQueue() {
        return QueueBuilder.durable(QUEUE).build();
    }

    /**
     * Binds the Queue to the Reservation Exchange
     * Only messages with the ROUTING_KEY will reach the queue
     */
    @Bean
    Binding userBinding() {
        return BindingBuilder
                .bind(userQueue())
                .to(reservationExchange())
                .with(ROUTING_KEY);
    }

    /**
     * Explicitly states a JSON converter for the Listener to use to convert messages to Java Objects
     */
    @Bean
    public JacksonJsonMessageConverter jacksonJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    /**
     * Configures the RabbitMQ listener to use the Jackson Message converter and sets the connection to listen on
     */
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
