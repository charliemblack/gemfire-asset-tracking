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

package demo.gemfire.asset.tracker.web;

import demo.gemfire.asset.tracker.lib.LocationEvent;
import demo.gemfire.asset.tracker.lib.SpaitalHelper;
import demo.gemfire.asset.tracker.lib.ToolBox;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.apache.geode.cache.lucene.LuceneQuery;
import org.apache.geode.cache.lucene.LuceneQueryException;
import org.apache.geode.cache.lucene.LuceneService;
import org.apache.geode.cache.lucene.LuceneServiceProvider;
import org.locationtech.jts.io.ParseException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

/**
 * Created by Charlie Black on 9/23/16.
 */
@RestController
@SpringBootApplication
public class GeospatialWebServer implements InitializingBean {


    @Value("${demo.GeospatialWebServer.locators:localhost[10334]}")
    private String locators;

    private ClientCache clientCache;
    @Override
    public void afterPropertiesSet() throws Exception {
        ClientCacheFactory clientCacheFactory = new ClientCacheFactory();
        ToolBox.configureDefaultClientPool(clientCacheFactory, locators);
        clientCache = clientCacheFactory.create();
        clientCache.createClientRegionFactory(ClientRegionShortcut.PROXY).create("geoSpatialRegion");
    }
    @RequestMapping(value = "/query", method = RequestMethod.GET, produces = "application/json")
    public Collection<LocationEvent> query(double minLng, double minLat, double maxLng, double maxLat) throws ParseException, LuceneQueryException {
        LuceneService luceneService = LuceneServiceProvider.get(clientCache);
        LuceneQuery<String, LocationEvent> luceneQuery = SpaitalHelper.findInRectangle("simpleIndex", "geoSpatialRegion", minLng, minLat, maxLng, maxLat, luceneService);
        return luceneQuery.findValues();
    }
    public static void main(String[] args) {
        SpringApplication.run(GeospatialWebServer.class, args);
    }
}
