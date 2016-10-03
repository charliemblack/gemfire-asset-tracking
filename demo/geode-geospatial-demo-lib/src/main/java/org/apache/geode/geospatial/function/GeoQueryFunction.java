package org.apache.geode.geospatial.function;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.client.ClientCacheFactory;
import com.gemstone.gemfire.cache.client.Pool;
import com.gemstone.gemfire.cache.execute.*;
import com.gemstone.gemfire.pdx.PdxInstance;
import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;
import org.apache.geode.geospatial.domain.LocationEvent;
import org.apache.geode.geospatial.index.GeospaitalIndex;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This is the server side function that uses the index to find items that are stored in Geode
 *
 * Created by Charlie Black on 9/23/16.
 */
public class GeoQueryFunction implements Function {
    public static final String ID = "geoQueryFunction";

    private int chunkSize = 1000;
    private GeospaitalIndex<Object, LocationEvent> geospaitalIndex;
    private Region<Object, PdxInstance> region;

    @Required
    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    @Required
    public void setGeospaitalIndex(GeospaitalIndex<Object, LocationEvent> geospaitalIndex) {
        this.geospaitalIndex = geospaitalIndex;
    }

    @Required
    public void setRegion(Region<Object, PdxInstance> region) {
        this.region = region;
    }

    @Override
    public void execute(FunctionContext context) {

        ResultSender<Collection<PdxInstance>> resultSender = context.getResultSender();
        try {
            String wellKownText = (String) context.getArguments();
            //Create a JTS object that we can test against.
            Geometry geometry = new WKTReader().read(wellKownText);

            ArrayList<Object> keys = new ArrayList<Object>(geospaitalIndex.query(geometry));

            List<List<Object>> partitionedKeys = Lists.partition(keys, chunkSize);
            for (List currKeySet : partitionedKeys) {
                resultSender.sendResult(new ArrayList<>(region.getAll(currKeySet).values()));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        resultSender.lastResult(null);

    }

    public static final List<PdxInstance> query(String wellKnowText) {

        //Use the default connection pool to make the request on.   If we were connected to more then one
        //distributed system we would have ask for right system.   Since we are connected to only one we are fine
        // with default.
        Pool pool = ClientCacheFactory.getAnyInstance().getDefaultPool();
        //Since the spatial data is large and it partitioned over N servers we need to query all of the servers at the
        //same time.   On servers tell GemFire to execute the specifed function on all servers.
        Execution execution = FunctionService.onServers(pool).withArgs(wellKnowText);
        //We cause the execution to happen asynchronously
        Collection<Collection<PdxInstance>> resultCollector = (Collection<Collection<PdxInstance>>) execution.execute(ID).getResult();

        ArrayList<PdxInstance> results = new ArrayList<>();

        resultCollector.forEach(pdxInstanceCollection -> {
            if (pdxInstanceCollection != null) {
                pdxInstanceCollection.forEach(locationEvent -> {
                    if (locationEvent != null) {
                        results.add(locationEvent);
                    }
                });
            }
        });

        return results;

    }

    @Override
    public boolean hasResult() {
        return true;
    }

    @Override
    public String getId() {
        return ID;
    }
}
