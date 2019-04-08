package com.bridgelabz.fundoo.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.bridgelabz.fundoo.response.Response;

/********************************************************************************************
 * Purpose : Contains the application configurations and bean definitions.
 *           
 * @author BridgeLabz/Sudhakar
 * @version 1.0
 * @since 26-02-2019
 *********************************************************************************************/
@Configuration
public class AppConfig {
	/**
	 * ModelMapper to map the DTO to actual model
	 * @return instance of ModelMapper
	 */
	@Bean
	public ModelMapper getModelMapper() {
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		return modelMapper;
	}

	/**
	 * BCrypt instance to encode the user password
	 * @return BCrypt instance 
	 */
	@Bean
	public BCryptPasswordEncoder encoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public Response getResponse()
	{
		return new Response();
	}
}
