package org.apache.geode.geospatial.domain;

import static org.locationtech.spatial4j.distance.DistanceUtils.EARTH_MEAN_RADIUS_MI;

import org.apache.geode.cache.lucene.LuceneQuery;
import org.apache.geode.cache.lucene.LuceneService;
import org.apache.lucene.document.Field;
import org.apache.lucene.search.Query;
import org.apache.lucene.spatial.query.SpatialArgs;
import org.apache.lucene.spatial.query.SpatialOperation;
import org.apache.lucene.spatial.vector.PointVectorStrategy;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.distance.DistanceUtils;
import org.locationtech.spatial4j.shape.Point;
import org.locationtech.spatial4j.shape.Shape;
import org.locationtech.spatial4j.shape.impl.GeoCircle;
import org.locationtech.spatial4j.shape.impl.PointImpl;

public class SpaitalHelper {
    private static final SpatialContext CONTEXT = SpatialContext.GEO;
    private static final PointVectorStrategy STRATEGY =
            new PointVectorStrategy(CONTEXT, "location", PointVectorStrategy.DEFAULT_FIELDTYPE);

    /**
     * Return a lucene query that finds all points within the given radius from the given point
     */
    public static Query findWithin(double longitude, double latitude, double radiusMiles) {
        // Covert the radius in miles to a radius in degrees
        double radiusDEG = DistanceUtils.dist2Degrees(radiusMiles, EARTH_MEAN_RADIUS_MI);

        // Create a query that looks for all points within a circle around the given point
        SpatialArgs args = new SpatialArgs(SpatialOperation.IsWithin,
                new GeoCircle(createPoint(longitude, latitude), radiusDEG, CONTEXT));
        return STRATEGY.makeQuery(args);
    }

    /**
     * Return a list of fields that should be added to lucene document to index the given point
     */
    public static Field[] getIndexableFields(double longitude, double latitude) {
        Point point = createPoint(longitude, latitude);
        return STRATEGY.createIndexableFields(point);
    }

    private static Point createPoint(double longitude, double latitude) {
        return new PointImpl(longitude, latitude, CONTEXT);
    }

    public static Query findDistanceForTheGivenCoord(double sourceLang, double sourceLat,
                                                     double radiusMiles) {
        double radiusDEG = DistanceUtils.dist2Degrees(radiusMiles, EARTH_MEAN_RADIUS_MI);
        SpatialArgs args = new SpatialArgs(SpatialOperation.Intersects,
                new GeoCircle(createPoint(sourceLang, sourceLat), radiusDEG, CONTEXT));
        return STRATEGY.makeQuery(args);
    }


    public static Query queryIntersectingPoints(double minLong, double minLat, double maxLong,
                                                double maxLat) {
        SpatialArgs args =
                new SpatialArgs(SpatialOperation.Intersects, getShape(minLong, minLat, maxLong, maxLat));
        return STRATEGY.makeQuery(args);
    }


    static Shape getShape(double minLong, double minLat, double maxLong, double maxLat) {
        return CONTEXT.getShapeFactory().rect((createPoint(minLong, minLat)),
                (createPoint(maxLong, maxLat)));

    }

    public static Query findLocationThatIsInsideTheRectangle(double minLong, double minLat,
                                                             double maxLong, double maxLat) {
        SpatialArgs args =
                new SpatialArgs(SpatialOperation.IsWithin, getShape(minLong, minLat, maxLong, maxLat));
        return STRATEGY.makeQuery(args);
    }

    public static LuceneQuery<String, LocationEvent> findInRectangle(String indexName, String regionName, double minLng, double minLat, double maxLng, double maxLat, LuceneService luceneService) {
        return luceneService.createLuceneQueryFactory().create(indexName, regionName, index -> findLocationThatIsInsideTheRectangle(minLng, minLat, maxLng, maxLat));
    }
}
