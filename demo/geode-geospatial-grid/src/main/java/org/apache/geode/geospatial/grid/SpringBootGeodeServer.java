package org.apache.geode.geospatial.grid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

import java.util.concurrent.CountDownLatch;

/**
 * Created by Charlie Black on 6/22/16.
 */
@SpringBootApplication
@ImportResource("classpath:/config/server-context.xml")
public class SpringBootGeodeServer {


    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(SpringBootGeodeServer.class);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await();
    }
}
