package org.apache.geode.geospatial.spring;

import com.gemstone.gemfire.cache.Region;
import org.springframework.core.convert.converter.Converter;

import java.util.concurrent.ConcurrentMap;


public class RegionToConcurrentMap implements Converter<Region<?, ?>, ConcurrentMap<?, ?>> {
    @Override
    public ConcurrentMap<?, ?> convert(Region<?, ?> source) {
        return source;
    }
}

