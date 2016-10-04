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
