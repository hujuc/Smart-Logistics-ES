package Operimus.SLMS;

import co.elastic.apm.attach.ElasticApmAttacher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SlmsApplication {
    public static void main(String[] args) {
        // Attach Elastic APM agent programmatically
        // This uses the elastic.apm.* properties from application.properties
        ElasticApmAttacher.attach();

        SpringApplication.run(SlmsApplication.class, args);
    }
}
