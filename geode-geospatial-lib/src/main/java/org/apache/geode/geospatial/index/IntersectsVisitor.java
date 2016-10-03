package org.apache.geode.geospatial.index;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.index.ItemVisitor;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Created by Charlie Black on 7/1/16.
 */
public class IntersectsVisitor implements ItemVisitor {

    private Set<Object> results = new UnifiedSet<Object>();
    private Geometry geometry;

    public IntersectsVisitor(Geometry geometry) {
        this.geometry = geometry;
    }


    public Collection<Object> getResults() {
        return results;
    }

    @Override
    public void visitItem(Object item) {
        Entry<Object, Geometry> entry = (Entry<Object, Geometry>) item;
        if (geometry.intersects(entry.getValue())) {
            results.add(entry.getKey());
        }
    }
}
