package org.apache.geode.geospatial.domain;

import com.gemstone.gemfire.pdx.PdxReader;
import com.gemstone.gemfire.pdx.PdxSerializable;
import com.gemstone.gemfire.pdx.PdxWriter;

/**
 * The primary data model for this project.
 * <p>
 * Created by Charlie Black on 7/11/16.
 */
public class LocationEvent implements PdxSerializable {
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
    public void toData(PdxWriter writer) {
        writer.writeString(UID, uid);
        writer.writeFloat(LAT, (float) lat);
        writer.writeFloat(LNG, (float) lng);
    }

    @Override
    public void fromData(PdxReader reader) {
        lng = reader.readFloat(LNG);
        lat = reader.readFloat(LAT);
        uid = reader.readString(UID);
    }
}
