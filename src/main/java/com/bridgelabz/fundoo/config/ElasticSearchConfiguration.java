package com.bridgelabz.fundoo.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticSearchConfiguration{

	@Value("${elasticsearch.host}")
	private String host;
	@Value("${elasticsearch.port}")
	private String port;
    
	@Bean(destroyMethod = "close")
	public RestHighLevelClient client() {
		RestHighLevelClient restHighLevelClient = new RestHighLevelClient(
				  RestClient.builder( new HttpHost(host, Integer.parseInt(port), "http")));
		return restHighLevelClient;
	}
}


