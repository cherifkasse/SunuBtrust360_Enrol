package com.SunuBtrust360_Enrol.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @author : Mamadou Cherif KASSE
 * @version : 1.0
 * @email : mamadoucherifkasse@gmail.com
 * @created : 30/04/2025, mercredi
 */
@Configuration
public class ConfigRestTemplate {
    @Bean
    public RestTemplate restTemplate() {
        CustomHttpRequestFactory customFact = new CustomHttpRequestFactory();
        return new RestTemplate(customFact.getClientHttpRequestFactory());
    }
}
