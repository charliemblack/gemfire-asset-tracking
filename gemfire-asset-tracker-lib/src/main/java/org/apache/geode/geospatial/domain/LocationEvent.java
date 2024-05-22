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

package org.apache.geode.geospatial.domain;


import org.apache.geode.DataSerializable;
import org.apache.geode.DataSerializer;
import org.apache.geode.pdx.PdxReader;
import org.apache.geode.pdx.PdxSerializable;
import org.apache.geode.pdx.PdxWriter;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * The primary data model for this project.
 * <p>
 * Created by Charlie Black on 7/11/16.
 */
public class LocationEvent implements DataSerializable {
    public static final String LAT = "lat";
    public static final String LNG = "lng";
    public static final String UID = "uid";
    private float lat = (float) 0.0;
    private float lng = (float) 0.0;
    private String uid;

    public LocationEvent(float lat, float lng, String uid) {
        this.lat = lat;
        this.lng = lng;
        this.uid = uid;
    }

    public LocationEvent(double lat, double lng, String uid) {
        this.lat = (float) lat;
        this.lng = (float) lng;
        this.uid = uid;
    }


    public LocationEvent() {

    }

    public float getLat() {
        return lat;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public float getLng() {
        return lng;
    }

    public void setLng(float lng) {
        this.lng = lng;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public void toData(DataOutput dataOutput) throws IOException {
        DataSerializer.writeFloat(lat, dataOutput);
        DataSerializer.writeFloat(lng, dataOutput);
        DataSerializer.writeString(uid, dataOutput);

    }

    @Override
    public void fromData(DataInput dataInput) throws IOException, ClassNotFoundException {
        lat = DataSerializer.readFloat(dataInput);
        lng = DataSerializer.readFloat(dataInput);
        uid = DataSerializer.readString(dataInput);
    }
}
