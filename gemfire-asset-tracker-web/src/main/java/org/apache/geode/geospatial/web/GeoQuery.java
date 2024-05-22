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

package org.apache.geode.geospatial.web;

import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.lucene.LuceneQuery;
import org.apache.geode.cache.lucene.LuceneQueryException;
import org.apache.geode.cache.lucene.LuceneService;
import org.apache.geode.cache.lucene.LuceneServiceProvider;
import org.apache.geode.geospatial.domain.LocationEvent;
import org.apache.geode.geospatial.domain.SpaitalHelper;
import org.apache.geode.pdx.PdxInstance;
import org.apache.lucene.search.Query;
import org.locationtech.jts.io.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.apache.geode.geospatial.domain.SpaitalHelper.findLocationThatIsInsideTheRectangle;

/**
 * Created by Charlie Black on 9/23/16.
 */
@RestController
public class GeoQuery implements InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(GeoQuery.class);

    @Value("${demo.locators}")
    private String locators;

    private ClientCache clientCache;

    @RequestMapping(value = "/query", method = RequestMethod.GET, produces = "application/json")
    public Collection<LocationEvent> query(double minLng, double minLat, double maxLng, double maxLat) throws ParseException, LuceneQueryException {
        LuceneService luceneService = LuceneServiceProvider.get(clientCache);
        LuceneQuery<String, LocationEvent> luceneQuery = SpaitalHelper.findInRectangle("simpleIndex", "geoSpatialRegion", minLng, minLat, maxLng, maxLat, luceneService);
        return luceneQuery.findValues();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("locators = " + locators);
        clientCache = new ClientCacheFactory()
                .addPoolLocator("localhost", 10334)
                .create();
    }
}
