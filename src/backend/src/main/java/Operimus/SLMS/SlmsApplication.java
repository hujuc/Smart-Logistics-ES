package Operimus.SLMS;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class SlmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(SlmsApplication.class, args);
    }
}
