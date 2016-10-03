package org.apache.geode.geospatial.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

/**
 * Created by Charlie Black on 9/23/16.
 */
@SpringBootApplication
@ImportResource("classpath:/config/client-context.xml")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
