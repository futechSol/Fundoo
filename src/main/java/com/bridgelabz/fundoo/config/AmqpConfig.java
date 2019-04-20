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

	@Value("${spring.rabbitmq.user.queue}")
	private String userQueue;
	@Value("${spring.rabbitmq.user.routingKey}")
	private String userRoutingKey;

	@Value("${spring.rabbitmq.note.queue}")
	private String noteQueue;
	@Value("${spring.rabbitmq.note.routingKey}")
	private String noteRoutingKey;

	@Bean
	Exchange exchage() {
		return new DirectExchange(exchange);
	}

	@Bean(name = "userQueue")
	Queue userQueue() {
		return new Queue(userQueue, false);
	}

	@Bean
	Binding userQueueBinding(Queue userQueue, DirectExchange exchange) {
		return BindingBuilder.bind(userQueue).to(exchange).with(userRoutingKey);
	}

	@Bean(name = "noteQueue")
	Queue noteQueue() {
		return new Queue(noteQueue, false);
	}
	
	@Bean
	Binding noteQueueBinding(Queue noteQueue, DirectExchange exchange) 
	{ 
		return  BindingBuilder.bind(noteQueue).to(exchange).with(noteRoutingKey);
	}

}
