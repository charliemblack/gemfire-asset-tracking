/*
 * Copyright [2016] Charlie Black
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
public class GeospatialSimulator {

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {

        new SpringApplicationBuilder(GeospatialSimulator.class)
                .web(false)
                .run();
    }
}
