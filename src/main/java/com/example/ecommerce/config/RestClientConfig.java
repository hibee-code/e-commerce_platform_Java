package com.example.ecommerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.example.ecommerce.payment.paystack.PaystackProperties;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient.Builder restClientBuilder(PaystackProperties paystackProperties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) paystackProperties.getConnectTimeout().toMillis());
        factory.setReadTimeout((int) paystackProperties.getReadTimeout().toMillis());
        return RestClient.builder().requestFactory(factory);
    }
}
