package Operimus.SLMS;

import co.elastic.apm.attach.ElasticApmAttacher;
import Operimus.SLMS.assistant.rag.PineconeProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(PineconeProperties.class)
public class SlmsApplication {
    public static void main(String[] args) {
        // Attach Elastic APM agent programmatically
        // This uses the elastic.apm.* properties from application.properties
        ElasticApmAttacher.attach();

        SpringApplication.run(SlmsApplication.class, args);
    }
}
