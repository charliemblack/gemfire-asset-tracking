package org.apache.geode.geospatial.spring;

import org.springframework.core.convert.converter.Converter;

import java.util.Map;


public class MapToMap implements Converter<Map<?, ?>, Map<?, ?>> {


    @Override
    public Map<?, ?> convert(Map<?, ?> source) {
        return source;
    }
}
