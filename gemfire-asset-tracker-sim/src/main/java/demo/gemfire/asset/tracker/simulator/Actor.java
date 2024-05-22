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

import net.sf.geographiclib.Geodesic;
import net.sf.geographiclib.GeodesicData;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.datum.Ellipsoid;

import java.util.concurrent.TimeUnit;

/**
 * Created by Charlie Black on 7/5/16.
 */
public class Actor {
    public static long MS_IN_HOUR = TimeUnit.HOURS.toMillis(1);
    public static final double METERS_IN_MILE = 1609.344;
    private static Geodesic geod;

    static {
        try {
            Ellipsoid ellipsoid = CRS.getEllipsoid(CRS.decode("EPSG:4326"));
            geod = new Geodesic(ellipsoid.getSemiMajorAxis(), 1 / ellipsoid.getInverseFlattening());
        } catch (FactoryException e) {
            e.printStackTrace();
        }
    }

    private Roads roads;
    private int currentIndex = 0;
    private Coordinate[] currentRoad;
    private Coordinate[] nextRoad;
    private double milesPerMillisecond = 55.0 / MS_IN_HOUR;
    private long timeToAdvance = System.currentTimeMillis();
    private String uid;

    public Actor(double mph, Roads roads, Coordinate[] currentRoad, String uid) {
        milesPerMillisecond = mph / MS_IN_HOUR;
        this.roads = roads;
        this.currentRoad = currentRoad;
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public double getMilesPerMillisecond() {
        return milesPerMillisecond;
    }

    public synchronized Coordinate currentEvent() {
        return getCurrentRoad()[currentIndex];
    }

    public synchronized Coordinate nextEvent() {
        Coordinate result;
        if (currentIndex + 1 >= getCurrentRoad().length) {
            result = getNextRoad()[0];
        } else {
            result = getCurrentRoad()[currentIndex + 1];
        }
        return result;
    }

    public synchronized void advance() {
        currentIndex++;
        if (currentIndex >= currentRoad.length) {
            currentIndex = 0;
            currentRoad = getNextRoad();
            nextRoad = null;
        }
        timeToAdvance = System.currentTimeMillis() + msToNextEvent();
    }

    private Coordinate[] getNextRoad() {
        if (nextRoad == null) {
            Coordinate[] curr = getCurrentRoad();
            nextRoad = roads.getNextRoad(curr[curr.length - 1]);
        }
        return nextRoad;
    }

    public Coordinate[] getCurrentRoad() {
        if (currentRoad == null) {
            currentRoad = roads.getRandomRoad();
            currentIndex = 0;
        }
        return currentRoad;
    }

    public long timeToAdvance() {
        return timeToAdvance;
    }

    private long msToNextEvent() {
        Coordinate coordinate1 = currentEvent();
        Coordinate coordinate2 = nextEvent();

        double distanceInMiles = distanceInMiles(coordinate1.y, coordinate1.x, coordinate2.y, coordinate2.x);
        //Calculates time required to ride a fixed distance in a given average speed. Formula: Time = Distance ÷ Speed
        return (int) (distanceInMiles / getMilesPerMillisecond());
    }

    private double distanceInMiles(double lat1, double long1, double lat2, double long2) {
        return distanceInMeters(lat1, long1, lat2, long2) / METERS_IN_MILE;
    }

    private double distanceInMeters(double lat1, double long1, double lat2, double long2) {
        GeodesicData g = geod.Inverse(lat1, long1, lat2, long2);
        return g.s12;
    }
}
