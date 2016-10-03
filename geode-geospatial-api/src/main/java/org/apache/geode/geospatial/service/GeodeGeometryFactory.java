package org.apache.geode.geospatial.service;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * Allows for the implementation of the index to be decoupled from the domain object.
 *
 * Created by Charlie Black on 6/23/16.
 */
public interface GeodeGeometryFactory<T> {

    /**
     * Initialize this factory with the configured Geometry Factory.
     * @param geometryFactory
     */
    void setGeometryFactory(GeometryFactory geometryFactory);

    /**
     * Will be called from the GeospatialIndex upsert method to find the Geometry of the passed in domain object.
     *
     * @param value
     * @return
     */
    Geometry getGeometry(T value);
}
