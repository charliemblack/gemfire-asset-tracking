package org.apache.geode.geospatial.index;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.index.ItemVisitor;
import org.apache.geode.geospatial.service.GeodeGeometryFactory;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * A geo spatial index based on a Quad Tree.  This implementation that allow fast removal of data via a key.
 */
/*
 *
 * Design Choice - Geometries that wrap the Poles and 180 / -180 aren't handled properly.  This was done  to
 *                 simplify the code. If this needs to handled correctly then developer needs to cut up the envelope
 *                 and insert and maintain the cut up envelopes.
 *
 * Created by Charlie Black on 6/23/16.
 */
public class BasicQuadTreeImpl implements GeospaitalIndex {

    private final ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = reentrantReadWriteLock.readLock();
    private final Lock writeLock = reentrantReadWriteLock.writeLock();

    private UnifiedMap<Object, Map<Object, Geometry>> quickRemove = new UnifiedMap<>();
    //16 levels is ~611 meters - (circumference of the earth in meters) / 2^16
    //Don't forget that we don't have to index the world.
    private byte maxDepth = 16;
    private Quad top = new Quad(new Envelope(-180, 180, -90, 90), maxDepth);
    private GeodeGeometryFactory<Object> geometryFactory;

    @Required
    @Override
    public void setGeodeGeometryFactory(GeodeGeometryFactory geodeGeometryFactory) {
        this.geometryFactory = geodeGeometryFactory;
    }


    @Override
    public void remove(Object key) {
        if (key != null) {
            writeLock.lock();
            try {
                Map<Object, Geometry> container = quickRemove.remove(key);
                if (container != null) {
                    container.remove(key);
                }
            } finally {
                writeLock.unlock();
            }
        }
    }

    @Override
    public void upsert(Object key, Object value) {
        Geometry geometry = geometryFactory.getGeometry(value);
        writeLock.lock();
        try {
            remove(key);
            top.insert(key, geometry);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public Collection query(Geometry geometry) {
        readLock.lock();
        try {
            IntersectsVisitor visitor = new IntersectsVisitor(geometry);
            top.query(geometry.getEnvelopeInternal(), visitor);
            return visitor.getResults();
        } finally {
            readLock.unlock();
        }
    }


    public Set keySet() {
        readLock.lock();
        try {
            return new HashSet<>(quickRemove.keySet());
        } finally {
            readLock.unlock();
        }
    }

    public void clear() {
        writeLock.lock();
        try {
            quickRemove.values().forEach(Map::clear);
            quickRemove.clear();
        } finally {
            writeLock.unlock();
        }
    }

    //--- From here down assumes that we are running in a protected context so no locks ---

    private class Quad {
        private byte quadTreeDepth;
        private Envelope levelEnvelope;
        private final Quad[] quads = new Quad[4];
        private UnifiedMap<Object, Geometry> items;

        public Quad(Envelope currSector, byte level) {
            this.quadTreeDepth = level;
            levelEnvelope = currSector;
        }

        boolean insert(Object key, Geometry geometry) {
            return insert(key, geometry.getEnvelopeInternal(), geometry);
        }

        //recursively add it to the tree
        boolean insert(Object key, Envelope envelope, Geometry geometry) {
            boolean found = false;
            if (quadTreeDepth >= 0) {
                for (int i = 3; i >= 0; i--) {
                    Quad quad = safeGetQuad(i);
                    //If a given quad contains a envelope then we need to decend until it doesn't contain or we hit
                    // the bottom of the tree.  For points we are going to hit the bottom of a tree.
                    if (quad.levelEnvelope.contains(envelope)) {
                        found = quad.insert(key, envelope, geometry);
                        break;
                    }
                }
            }
            //We either hit bottom or we have and envelope that doesn't fit any lower.
            if (!found && levelEnvelope.contains(envelope)) {
                UnifiedMap<Object, Geometry> items = safeGetItems();
                items.put(key, geometry);
                quickRemove.put(key, items);
                found = true;
            }
            return found;
        }

        //Lazy make the storage of items.
        private UnifiedMap<Object, Geometry> safeGetItems() {
            if (items == null) {
                items = new UnifiedMap<>();
            }
            return items;
        }

        //Lazy make the quad tree
        private Quad safeGetQuad(int quad) {
            Quad returnValue = quads[quad];
            if (returnValue == null) {
                synchronized (quads) {
                    if (quads[quad] == null) {
                        Envelope envelopeForQuad = makeEnvelopeForQuad(quad);
                        returnValue = quads[quad] = new Quad(envelopeForQuad, (byte) (quadTreeDepth - 1));
                    } else {
                        //another thread beat curr thread here
                        returnValue = quads[quad];
                    }
                }
            }
            //we have either gotten the instance or created one.
            return returnValue;
        }

        public void query(Envelope queryEnvelope, ItemVisitor visitor) {
            if (items != null && !items.isEmpty()) {
                if (queryEnvelope.intersects(levelEnvelope)) {
                    //If the item doesn't interest with the Quad Envelope then it won't be in this level
                    items.entrySet().forEach(visitor::visitItem);
                }
            }
            for (Quad quad : quads) {
                if (quad != null) {
                    if (quad.levelEnvelope.intersects(queryEnvelope)) {
                        //decend if the query envelop intersets.
                        quad.query(queryEnvelope, visitor);
                    }
                }
            }
        }

        /**
         * 0 | 1
         * --+--
         * 2 | 3
         * <p>
         * or another way to think about it:
         * <p>
         * minX, maxY | maxX, maxY
         * -----------+-----------
         * minX, minY | maxX, minY
         * <p>
         * X = lng
         * Y = lat
         */
        public Envelope makeEnvelopeForQuad(int area) {
            Envelope quad = null;
            double midY = (levelEnvelope.getMaxY() + levelEnvelope.getMinY()) / 2.0;
            double midX = (levelEnvelope.getMaxX() + levelEnvelope.getMinX()) / 2.0;

            switch (area) {
                case 0:
                    quad = new Envelope(levelEnvelope.getMinX(), midX, levelEnvelope.getMaxY(), midY);
                    break;
                case 1:
                    quad = new Envelope(levelEnvelope.getMaxX(), midX, levelEnvelope.getMaxY(), midY);
                    break;
                case 2:
                    quad = new Envelope(levelEnvelope.getMinX(), midX, levelEnvelope.getMinY(), midY);
                    break;
                case 3:
                    quad = new Envelope(levelEnvelope.getMaxX(), midX, levelEnvelope.getMinY(), midY);
                    break;
            }
            return quad;
        }
    }
}
