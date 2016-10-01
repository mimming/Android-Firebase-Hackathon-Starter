package com.mimming.hacks.starter;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by mimming on 10/1/16.
 *
 *  An adapter for com.google.android.gms.maps.model.LatLng
 *  so that I can do cleaner Firebase serialization
 */

public class LatLngWrapper {
    public double latitude;
    public double longitude;

    public LatLngWrapper(LatLng latLng) {
        this.latitude = latLng.latitude;
        this.longitude = latLng.longitude;
    }
    public LatLngWrapper() {
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    LatLng toLatLng() {
        return new LatLng(this.latitude, this.longitude);
    }
}
