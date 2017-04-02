package com.ademir.mapsapp.models;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by ademir on 02/04/17.
 */

public class Place {

    public String name;
    public String address;
    public String attributions;
    public LatLng latLng;

    public Place(String name, String address, String attributions, LatLng latLng) {
        this.name = name;
        this.address = address;
        this.attributions = attributions;
        this.latLng = latLng;
    }

}
