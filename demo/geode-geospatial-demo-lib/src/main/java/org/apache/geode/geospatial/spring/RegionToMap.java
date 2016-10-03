package org.apache.geode.geospatial.spring;

import com.gemstone.gemfire.cache.Region;
import org.springframework.core.convert.converter.Converter;

import java.util.Map;


public class RegionToMap implements Converter<Region<?, ?>, Map<?, ?>> {

    @Override
    public Map<?, ?> convert(Region<?, ?> source) {
        return source;
    }
}
