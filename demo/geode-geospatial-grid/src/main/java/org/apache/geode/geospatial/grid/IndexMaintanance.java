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

import org.apache.geode.cache.Operation;
import org.apache.geode.cache.asyncqueue.AsyncEvent;
import org.apache.geode.cache.asyncqueue.AsyncEventListener;
import org.apache.geode.geospatial.index.GeospatialIndex;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

/**
 * With partitioned regions only the primary data node will fire events.
 *
 * We are using a AsyncEventListener to make sure we are processing the events in order.
 * Created by Charlie Black on 6/23/16.
 */
public class IndexMaintanance implements AsyncEventListener {

    private GeospatialIndex geospatialIndex;

    @Required
    public void setGeospatialIndex(GeospatialIndex geospatialIndex) {
        this.geospatialIndex = geospatialIndex;
    }

    /**
     * Process the list of <code>AsyncEvent</code>s. This method will
     * asynchronously be called when events are queued to be processed.
     * The size of the list will be up to batch size events where batch
     * size is defined in the <code>AsyncEventQueueFactory</code>.
     *
     * @param events The list of <code>AsyncEvent</code> to process
     * @return boolean    True represents whether the events were successfully processed,
     * false otherwise.
     */
    @Override
    public boolean processEvents(List<AsyncEvent> events) {
        for(AsyncEvent curr: events){
            Operation operation = curr.getOperation();
            if(operation.isDestroy()){
                geospatialIndex.remove(curr.getKey());
            } else  if(operation.isCreate() || operation.isUpdate()){
                geospatialIndex.upsert(curr.getKey(),curr.getDeserializedValue());
            }
        }
        return true;
    }

    /**
     * Called when the region containing this callback is closed or destroyed, when
     * the cache is closed, or when a callback is removed from a region
     * using an <code>AttributesMutator</code>.
     * <p>
     * <p>Implementations should cleanup any external
     * resources such as database connections. Any runtime exceptions this method
     * throws will be logged.
     * <p>
     * <p>It is possible for this method to be called multiple times on a single
     * callback instance, so implementations must be tolerant of this.
     *
     * @see Cache#close()
     * @see Region#close
     * @see Region#localDestroyRegion()
     * @see Region#destroyRegion()
     * @see AttributesMutator
     */
    @Override
    public void close() {

    }
}
