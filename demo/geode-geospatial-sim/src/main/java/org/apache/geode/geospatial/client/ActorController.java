package org.apache.geode.geospatial.client;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.vividsolutions.jts.geom.Coordinate;
import org.apache.geode.geospatial.domain.LocationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import java.util.Comparator;
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
    private final MetricRegistry metrics = new MetricRegistry();
    private final Histogram actorDelay = metrics.histogram("actorDelay");
    private String beanName;
    private long newActorTimeout = 10;

    public void setNewActorTimeout(long newActorTimeout) {
        this.newActorTimeout = newActorTimeout;
    }
    @Required
    public void setGeoRegion(ConcurrentMap geoRegion) {
        this.geoRegion = geoRegion;
    }

    @Required
    public void setRoads(Roads roads) {
        this.roads = roads;
    }

    @Required
    public void setNumberOfActors(int numberOfActors) {
        this.numberOfActors = numberOfActors;
    }

    @Required
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
                    actorDelay.update(currentDelay);
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
        JmxReporter reporter = JmxReporter.forRegistry(metrics).inDomain(beanName).build();
        reporter.start();
    }
}
