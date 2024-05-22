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

package org.apache.geode.geospatial.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Iterator;
import java.util.Properties;
import java.util.ServiceLoader;

/**
 * Created by Charlie Black on 7/26/16.
 */
public class ToolBox {
    private static final Logger logger = LoggerFactory.getLogger(ToolBox.class);

    private ToolBox() {
    }

   /* public static void configureDefaultClientPool(ClientCacheFactory clientCacheFactory, String locators) {
        StringTokenizer stringTokenizer = new StringTokenizer(locators, ",");
        clientCacheFactory.setPoolMaxConnections(-1);
        clientCacheFactory.setPoolPRSingleHopEnabled(true);

        while (stringTokenizer.hasMoreTokens()) {
            String curr = stringTokenizer.nextToken();
            DistributionLocatorId locatorId = new DistributionLocatorId(curr);
            String addr = locatorId.getBindAddress();
            if (addr != null && addr.trim().length() > 0) {
                clientCacheFactory.addPoolLocator(addr, locatorId.getPort());
            } else {
                clientCacheFactory.addPoolLocator(locatorId.getHost().getHostName(), locatorId.getPort());
            }
        }
        clientCacheFactory.create();
    }
*/
    public static <T> T getService(Class clazz) {
        ServiceLoader<T> loader = ServiceLoader.load(clazz);

        T returnValue = null;

        Iterator<T> it = loader.iterator();
        //In theory the service providers are loaded lazily - so if there is a problem with a given service provider we
        // will log it and try the next.
        while (it.hasNext()) {
            try {
                returnValue = it.next();
                if (returnValue != null) {
                    break;
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                logger.error("Trying to find another ", clazz.getName());
            }
        }
        return returnValue;
    }

    public static Properties testAndSetProperty(Properties properties, String property, String value) {
        if (properties != null && StringUtils.hasText(value)) {
            properties.setProperty(property, value);
        }
        return properties;
    }
}
