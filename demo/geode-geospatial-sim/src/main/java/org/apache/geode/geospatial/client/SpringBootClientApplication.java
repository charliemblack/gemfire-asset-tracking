package org.apache.geode.geospatial.client;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ImportResource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;


/**
 * We are going to let GemFire start Spring.
 * <p>
 * Created by Charlie Black on 7/1/16.
 */
@SpringBootApplication
@ImportResource("classpath:/config/client-context.xml")
public class SpringBootClientApplication {

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {

        new SpringApplicationBuilder(SpringBootClientApplication.class)
                .web(false)
                .run();
    }
}
