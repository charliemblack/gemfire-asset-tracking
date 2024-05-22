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

import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.index.ItemVisitor;

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
