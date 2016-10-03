package org.apache.geode.geospatial.web;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.gemstone.gemfire.pdx.PdxInstance;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;

/**
 * Created by Charlie Black on 9/23/16.
 */
@JsonComponent
public class PdxJSonSerializers {
    public static class Serializer extends JsonSerializer<PdxInstance> {
        @Override
        public void serialize(PdxInstance value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
            gen.writeStartObject();
            for (String name : value.getFieldNames()) {
                Object object = value.getField(name);
                gen.writeObjectField(name, object);
            }
            gen.writeEndObject();
        }
    }
}
