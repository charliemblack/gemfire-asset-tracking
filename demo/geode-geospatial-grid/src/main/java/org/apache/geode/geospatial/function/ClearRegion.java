package org.apache.geode.geospatial.function;

import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.execute.Function;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.gemstone.gemfire.cache.partition.PartitionRegionHelper;
import com.google.common.collect.Lists;

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
