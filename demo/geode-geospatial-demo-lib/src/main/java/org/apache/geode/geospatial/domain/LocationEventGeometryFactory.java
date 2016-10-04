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
