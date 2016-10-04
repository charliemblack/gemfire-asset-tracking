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

package org.apache.geode.geospatial.grid;

import com.gemstone.gemfire.cache.CacheWriterException;
import com.gemstone.gemfire.cache.EntryEvent;
import com.gemstone.gemfire.cache.util.CacheWriterAdapter;
import org.apache.geode.geospatial.index.GeospaitalIndex;
import org.springframework.beans.factory.annotation.Required;

/**
 * With partitioned regions only the primary data node will fire events.
 *
 * We are using a cache writer to make sure we are processing the events in order.
 * Created by Charlie Black on 6/23/16.
 */
public class IndexMaintanance extends CacheWriterAdapter{

    private GeospaitalIndex geospaitalIndex;

    @Override
    public void beforeCreate(EntryEvent event) throws CacheWriterException {
        geospaitalIndex.upsert(event.getKey(), event.getNewValue());
    }

    @Override
    public void beforeDestroy(EntryEvent event) throws CacheWriterException {
        geospaitalIndex.remove(event.getKey());

    }

    @Override
    public void beforeUpdate(EntryEvent event) throws CacheWriterException {
        geospaitalIndex.upsert(event.getKey(), event.getNewValue());
    }

    @Required
    public void setGeospaitalIndex(GeospaitalIndex geospaitalIndex) {
        this.geospaitalIndex = geospaitalIndex;
    }

}
