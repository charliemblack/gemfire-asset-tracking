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

package org.apache.geode.geospatial.function;

import com.google.common.collect.Lists;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionContext;
import org.apache.geode.cache.partition.PartitionRegionHelper;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Charlie Black on 9/28/16.
 */
public class ClearRegion implements Function {
    public static final String ID = "clearRegion";

    @Override
    public boolean hasResult() {
        return true;
    }

    @Override
    public void execute(FunctionContext context) {

        String regionName = ((String[]) context.getArguments())[0];
        Region region = CacheFactory.getAnyInstance().getRegion(regionName);

        if (PartitionRegionHelper.isPartitionedRegion(region)) {
            region = PartitionRegionHelper.getLocalPrimaryData(region);
        }
        int size = region.size();
        final Region lambdaRegion = region;
        Collection<Collection> partitionedKeys = Lists.partition(new ArrayList(region.keySet()), 1000);
        partitionedKeys.forEach(keySet -> lambdaRegion.removeAll(keySet));
        context.getResultSender().lastResult("Removed " + size + " keys.");
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public boolean optimizeForWrite() {
        return true;
    }
}
