package org.apache.geode.geospatial.spring;

import com.gemstone.gemfire.cache.Region;
import org.springframework.core.convert.converter.Converter;

import java.util.Map;


public class MapToRegion implements Converter<Map<?, ?>, Region<?, ?>> {

    @Override
    public Region<?, ?> convert(Map<?, ?> source) {
        if (source instanceof Region) {
            return (Region) source;
        }
        return null;
    }
}
