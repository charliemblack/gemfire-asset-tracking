package org.apache.geode.geospatial.domain;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import org.apache.geode.geospatial.service.GeodeGeometryFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * Created by Charlie Black on 7/11/16.
 */
public class LocationEventGeometryFactory implements GeodeGeometryFactory<LocationEvent> {

    private GeometryFactory geometryFactory = new GeometryFactory();

    @Required
    @Override
    public void setGeometryFactory(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
    }

    @Override
    public Geometry getGeometry(LocationEvent locationEvent) {
        Point location = geometryFactory.createPoint(new Coordinate(locationEvent.getLng(), locationEvent.getLat()));
        return location;
    }
}
