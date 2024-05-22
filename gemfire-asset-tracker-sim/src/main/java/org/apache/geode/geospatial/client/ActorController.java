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

import org.apache.geode.geospatial.domain.LocationEvent;
import org.locationtech.jts.geom.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;

import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by Charlie Black on 7/7/16.
 */
public class ActorController implements Runnable, InitializingBean, BeanNameAware {

    private static final Logger logger = LoggerFactory.getLogger(ActorController.class);

    private ConcurrentMap geoRegion;
    private Roads roads;
    private int numberOfActors = 1000;
    private int numberOfSimulators = 4;
    private String beanName;
    private long newActorTimeout = 10;

    public void setNewActorTimeout(long newActorTimeout) {
        this.newActorTimeout = newActorTimeout;
    }
    public void setGeoRegion(ConcurrentMap geoRegion) {
        this.geoRegion = geoRegion;
    }

    public void setRoads(Roads roads) {
        this.roads = roads;
    }

    public void setNumberOfActors(int numberOfActors) {
        this.numberOfActors = numberOfActors;
    }

    public void setNumberOfSimulators(int numberOfSimulators) {
        this.numberOfSimulators = numberOfSimulators;
    }

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
                            Thread.sleep(1);
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
    public void setBeanName(String name) {
        beanName = name;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }
}
