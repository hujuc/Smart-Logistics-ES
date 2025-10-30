package Operimus.SLMS.business_layer.order_shipment_module.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class CarrierIntegrationConfig {

    @Bean
    @Qualifier("shippoRestTemplate")
    RestTemplate shippoRestTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    @Qualifier("shipEngineRestTemplate")
    RestTemplate shipEngineRestTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}
