package org.apache.geode.geospatial.spring;

import com.gemstone.gemfire.cache.Region;
import org.springframework.core.convert.converter.Converter;


public class RegionToRegion implements Converter<Region<?, ?>, Region<?, ?>> {

    @Override
    public Region<?, ?> convert(Region<?, ?> source) {
        return source;
    }
}
