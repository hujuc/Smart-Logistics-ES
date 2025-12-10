package Operimus.SLMS;

import co.elastic.apm.attach.ElasticApmAttacher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@EnableScheduling
public class SlmsApplication {
    public static void main(String[] args) {
        // Attach Elastic APM agent programmatically with explicit configuration
        // Note: Must be done before Spring Boot starts, so we read from environment variables
        Map<String, String> apmConfig = new HashMap<>();

        // Read from environment variables (set by Docker Compose)
        String serverUrl = System.getenv("ELASTIC_APM_SERVER_URL");
        String secretToken = System.getenv("ELASTIC_APM_SECRET_TOKEN");
        String deploymentEnv = System.getenv("DEPLOYMENT_ENV");

        if (serverUrl != null && !serverUrl.isEmpty()) {
            apmConfig.put("server_url", serverUrl);
            apmConfig.put("secret_token", secretToken != null ? secretToken : "");
            apmConfig.put("service_name", "SLMS");  // From spring.application.name
            apmConfig.put("environment", deploymentEnv != null ? deploymentEnv : "development");
            apmConfig.put("application_packages", "Operimus.SLMS");
            apmConfig.put("log_level", "DEBUG");

            ElasticApmAttacher.attach(apmConfig);
        }

        SpringApplication.run(SlmsApplication.class, args);
    }
}
