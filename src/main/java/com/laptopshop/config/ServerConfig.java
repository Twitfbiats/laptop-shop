package com.laptopshop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableScheduling
@PropertySource("classpath:paypal.properties")
public class ServerConfig 
{
    @Bean
    public RestTemplate restTemplate()
    {
        return new RestTemplate();
    }
}