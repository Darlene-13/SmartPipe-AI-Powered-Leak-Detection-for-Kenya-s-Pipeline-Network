package io.github.darlene.leakdetectionapplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableScheduling
public class LeakDetectionApplication {

    public static void main(String[] args) {
        SpringApplication.run(LeakDetectionApplication.class, args);
    }

}
