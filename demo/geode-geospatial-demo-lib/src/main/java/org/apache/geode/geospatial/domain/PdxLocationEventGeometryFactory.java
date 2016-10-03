package org.apache.geode.geospatial.domain;

import com.gemstone.gemfire.pdx.PdxInstance;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.apache.geode.geospatial.service.GeodeGeometryFactory;
import org.springframework.beans.factory.annotation.Required;

import static org.apache.geode.geospatial.domain.LocationEvent.LAT;
import static org.apache.geode.geospatial.domain.LocationEvent.LNG;

/**
 * Created by Charlie Black on 9/21/16.
 */
public class PdxLocationEventGeometryFactory implements GeodeGeometryFactory<PdxInstance> {

    private GeometryFactory geometryFactory;

    @Required
    @Override
    public void setGeometryFactory(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
    }

    @Override
    public Geometry getGeometry(PdxInstance value) {
        Geometry geometry = geometryFactory.createPoint(new Coordinate(((Number) value.getField(LNG)).doubleValue(), ((Number) value.getField(LAT)).doubleValue()));
        return geometry;
    }
}
