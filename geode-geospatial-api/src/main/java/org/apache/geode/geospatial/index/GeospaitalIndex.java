package org.apache.geode.geospatial.index;

import com.vividsolutions.jts.geom.Geometry;
import org.apache.geode.geospatial.service.GeodeGeometryFactory;

import java.util.Collection;

/**
 * The geo-spatial index interface that is left up to the implementation to specify what type of indexing to do.
 *
 * Created by Charlie Black on 7/1/16.
 */
public interface GeospaitalIndex<K, V> {
    /**
     * Remove a key from the index.
     *
     * @param key
     */
    void remove(K key);

    /**
     * Update or insert an item to be indexed.   The implementation should use the value to call the
     * GeodeGeometryFactory to get the Geometry for indexing.   Then the key will be used to remove the value from index
     * control
     *
     * @param key
     * @param value
     */
    void upsert(K key, V value);

    /**
     * Query the geometry using another Geometry.
     *
     * @param geometry
     * @return
     */
    Collection<K> query(Geometry geometry);

    /**
     * Allows for the "tailorization" of the index to handle specific domain types.
     *
     * @param geodeGeometryFactory
     */
    void setGeodeGeometryFactory(GeodeGeometryFactory<V> geodeGeometryFactory);

    /**
     * Clear the index of data under its control.
     */
    void clear();
}
