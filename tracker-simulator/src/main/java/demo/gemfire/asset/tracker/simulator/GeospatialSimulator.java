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

package demo.gemfire.asset.tracker.simulator;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.client.ClientRegionShortcut;
import demo.gemfire.asset.tracker.lib.LocationEvent;
import demo.gemfire.asset.tracker.lib.ToolBox;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * Created by Charlie Black on 7/1/16.
 */
@SpringBootApplication
public class GeospatialSimulator implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(GeospatialSimulator.class);

    @Value("${demo.GeospatialSimulator.batchSize:100}")
    private int batchSize;
    @Value("${demo.GeospatialSimulator.batchTimeOut:100}")
    private int batchTimeOut;
    @Value("${demo.GeospatialSimulator.roadsFileName:data/Trknet2011.kmz}")
    private String roadsFileName;
    @Value("${demo.GeospatialSimulator.regionName:geoSpatialRegion}")
    private String geoSpatialRegionName;
    private CachingPutAllMap geoRegion;
    private Roads roads;
    @Value("${demo.GeospatialSimulator.numberOfActors:1000000}")
    private int numberOfActors;
    @Value("${demo.GeospatialSimulator.numberOfSimulators:10}")
    private int numberOfSimulators;
    @Value("${demo.GeospatialSimulator.newActorTimeout:1}")
    private long newActorTimeout;
    @Value("${demo.GeospatialSimulator.locators:localhost[10334]}")
    private String locators;

    private GeometryFactory geometryFactory = new GeometryFactory();

    public void run() {
        final PriorityBlockingQueue<Actor> priorityQueue = new PriorityBlockingQueue<>(numberOfActors, (Comparator<Actor>) (o1, o2) -> (int) (o1.timeToAdvance() - o2.timeToAdvance()));

        //Slam in a couple tracks to keep the simulators busy
        int initialCount = numberOfSimulators * 2;
        for (int i = 0; i < initialCount; i++) {
            addActorToQueue(priorityQueue, i);
        }

        for (int i = 0; i < numberOfSimulators; i++) {
            new Thread(() -> {
                while (true) {
                    Actor actor = priorityQueue.poll();
                    long currentDelay = actor.timeToAdvance() - System.currentTimeMillis();
                    if (currentDelay <= 0) {
                        try {
                            actor.advance();
                            Coordinate coordinate = actor.currentEvent();

                            LocationEvent locationEvent = new LocationEvent(coordinate.y, coordinate.x, actor.getUid());
                            geoRegion.put(locationEvent.getUid(), locationEvent);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                        priorityQueue.add(actor);
                    } else {
                        priorityQueue.add(actor);
                        try {
                            Thread.sleep(currentDelay);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }

        for (int i = initialCount; i < numberOfActors; i++) {
            //Now slowly trickle in more actors until we get up to the number of requested actors.
            //if we don't trickle them in then they are clustered at various starting points.
            try {
                Thread.sleep(newActorTimeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            addActorToQueue(priorityQueue, i);
            if(i %1000 == 0){
                logger.info("Injected {} drivers @ {}", i, new Date());
            }
        }
    }

    private void addActorToQueue(PriorityBlockingQueue<Actor> priorityQueue, int i) {
        //Randomly have the tracks move between 50 to 70 MPH
        priorityQueue.add(new Actor(Math.random() * 20 + 50, roads, roads.getRandomRoad(), Integer.toString(i)));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        roads = new Roads(geometryFactory, roadsFileName);
        ClientCacheFactory clientCacheFactory = new ClientCacheFactory();
        ToolBox.configureDefaultClientPool(clientCacheFactory, locators);
        ClientCache clientCache = clientCacheFactory.create();

        Region region = clientCache.createClientRegionFactory(ClientRegionShortcut.PROXY).create(geoSpatialRegionName);
        geoRegion = new CachingPutAllMap();
        geoRegion.setCallerSends(false);
        geoRegion.setBatchSize(batchSize);
        geoRegion.setTimeout(batchTimeOut);
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(256);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setThreadNamePrefix("SimulatorPool-");
        executor.initialize();
        geoRegion.setExecutor(executor);
        geoRegion.setWrappedMap(region);

        Thread thread = new Thread(this::run);
        thread.setDaemon(false);
        thread.setName("Sim-Initializer");
        thread.start();
    }
    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {

        new SpringApplicationBuilder(GeospatialSimulator.class)
                .run();
    }
}
