package org.apache.geode.geospatial.client;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.geode.geospatial.index.BasicQuadTreeImpl;
import org.apache.geode.geospatial.index.GeospaitalIndex;
import org.apache.geode.geospatial.service.GeodeGeometryFactory;
import org.geotools.kml.KMLConfiguration;
import org.geotools.referencing.CRS;
import org.geotools.referencing.GeodeticCalculator;
import org.geotools.xml.Parser;
import org.opengis.feature.simple.SimpleFeature;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.springframework.util.Assert.notNull;

/**
 * Created by Charlie Black on 7/5/16.
 */
public class Roads {
    private static final int CIRCLE_APPROXIMATION = 32;
    private GeospaitalIndex<Integer, Geometry> geospaitalIndex = new BasicQuadTreeImpl();
    private Map<Integer, Geometry> multimap = new HashMap<>();
    private GeometryFactory geometryFactory = new GeometryFactory();
    private Random random = new Random(System.currentTimeMillis());
    private AtomicInteger count = new AtomicInteger(0);

    public Roads(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
        geospaitalIndex.setGeodeGeometryFactory(new GeodeGeometryFactory<Geometry>() {

            @Override
            public void setGeometryFactory(GeometryFactory geometryFactory) {

            }

            @Override
            public Geometry getGeometry(Geometry value) {
                return geometryFactory.createPoint(value.getCoordinate());
            }
        });
    }

    /**
     * Find the a random road within a 5 mile radius.
     *
     * @param coordinate
     * @return
     */

    public Coordinate[] getNextRoad(Coordinate coordinate) {

        Coordinate[] result = null;
        Collection<Integer> coordinates = geospaitalIndex.query(calculatePolygon(coordinate, 5 * Actor.METERS_IN_MILE));
        if (coordinates != null && coordinates.size() > 0) {
            Integer[] array = coordinates.toArray(new Integer[coordinates.size()]);
            int index = random.nextInt(array.length);
            result = multimap.get(array[index]).getCoordinates();
        }
        return result;
    }

    /**
     * Get any road randomly.
     *
     * @return
     */
    public Coordinate[] getRandomRoad() {
        List<Geometry> geometries = new ArrayList<>(multimap.values());
        return geometries.get(random.nextInt(geometries.size())).getCoordinates();
    }

    /**
     * Load the roads from a file.  The roads loaded will be added to the set of roads.
     *
     * The loaded will add in a bi-directional path.
     *
     * Assumes the roads will be defined in a KMZ file.
     *
     * @param roadsFileName
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public void setRoads(String roadsFileName) throws IOException, ParserConfigurationException, SAXException {

        SimpleFeature featureSet = null;
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(new File(roadsFileName)))) {
            for (ZipEntry zipEntry = zipInputStream.getNextEntry(); zipEntry != null; zipEntry = zipInputStream.getNextEntry()) {
                if (zipEntry.getName().toLowerCase().endsWith(".kml")) {
                    KMLConfiguration kmlConfiguration = new KMLConfiguration();
                    Parser parser = new Parser(kmlConfiguration);
                    featureSet = (SimpleFeature) parser.parse(zipInputStream);
                    //just one KML feature and we punch out.
                    break;
                }
            }
        }
        notNull(featureSet, "Didn't find any features in " + roadsFileName);
        ArrayList<Geometry> geometries = new ArrayList<>();
        findAllGeometries(geometries, featureSet);

        addRoads(geometries);

        ArrayList<Geometry> reverse = new ArrayList<>();

        for (Geometry curr : geometries) {
            Coordinate[] toReverse = Arrays.copyOf(curr.getCoordinates(), curr.getCoordinates().length);
            CollectionUtils.reverseArray(toReverse);
            reverse.add(geometryFactory.createLineString(toReverse));
        }
        addRoads(reverse);

    }

    private void addRoads(Collection<Geometry> roadGeometries) {
        for (Geometry geometry : roadGeometries) {
            int road = count.incrementAndGet();
            multimap.put(road, geometry);
            geospaitalIndex.upsert(road, geometry);
        }
    }


    private Coordinate convert(Point2D point2D) {
        return new Coordinate(point2D.getX(), point2D.getY());
    }

    private Geometry calculatePolygon(Coordinate coordinate, double radiusInMeters) {
        return calculatePolygon(coordinate.y, coordinate.x, radiusInMeters);
    }

    private Geometry calculatePolygon(double lat, double lng, double radiusInMeters) {

        Geometry returnValue = null;
        try {
            GeodeticCalculator calculator = new GeodeticCalculator(CRS.decode("EPSG:4326"));
            double azimuthInc = 360 / CIRCLE_APPROXIMATION;

            calculator.setStartingGeographicPoint(lng, lat);
            calculator.setDirection(0, radiusInMeters);
            Coordinate start = convert(calculator.getDestinationGeographicPoint());
            List<Coordinate> coordinates = new ArrayList<>();
            coordinates.add(start);
            for (double i = azimuthInc; i < 360; i += azimuthInc) {
                calculator.setDirection(i, radiusInMeters);
                coordinates.add(convert(calculator.getDestinationGeographicPoint()));
            }
            coordinates.add(start);

            returnValue = geometryFactory.createPolygon(coordinates.toArray(new Coordinate[coordinates.size()]));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnValue;
    }


    /**
     * Recursively find all of the roads in the file.   Why?  The roads can be in folders and we have to navigate the
     * folders.
     *
     * @param geometries
     * @param featureSet
     */
    private void findAllGeometries(List<Geometry> geometries, SimpleFeature featureSet) {
        List<SimpleFeature> features = (List<SimpleFeature>) featureSet.getAttribute("Feature");
        if (features != null) {
            for (SimpleFeature curr : features) {
                findAllGeometries(geometries, curr);
            }
        }
        Geometry geometry = (Geometry) featureSet.getAttribute("Geometry");
        if (geometry != null && geometry.getCoordinates().length > 2) {
            geometries.add(geometry);
        }
    }
}
