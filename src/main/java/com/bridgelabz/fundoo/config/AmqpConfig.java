package com.bridgelabz.fundoo.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AmqpConfig {
	
	@Value("${spring.rabbitmq.template.exchange}")
	private String exchange;
	@Value("${spring.rabbitmq.template.default-receive-queue}")
	private String queue;
	@Value("${spring.rabbitmq.template.routing-key}")
	private String routingKey;
	
	@Bean
	Exchange exchage() {
		return new DirectExchange(exchange);
	}

	@Bean
	Queue queue() {
		return new Queue(queue,false);
	}

	@Bean
	Binding binding(Queue queue, DirectExchange exchange) {
		return BindingBuilder.bind(queue).to(exchange).with(routingKey);
	}
}
