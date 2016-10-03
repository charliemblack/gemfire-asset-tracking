package org.apache.geode.geospatial.web;

import com.gemstone.gemfire.pdx.PdxInstance;
import org.apache.geode.geospatial.function.GeoQueryFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by Charlie Black on 9/23/16.
 */
@RestController
public class GeoQuery {
    private static final Logger logger = LoggerFactory.getLogger(GeoQuery.class);

    @RequestMapping(value = "/query", method = RequestMethod.GET, produces = "application/json")
    public List<PdxInstance> query(String wktPolygon) {
        logger.info("the wkt - {}", wktPolygon);
        List<PdxInstance> result = GeoQueryFunction.query(wktPolygon);
        logger.info("result size  = {}", result.size());
        return result;
    }
}
