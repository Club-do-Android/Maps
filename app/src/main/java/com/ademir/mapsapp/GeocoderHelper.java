package com.ademir.mapsapp;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by ademir on 08/04/17.
 */

public class GeocoderHelper {

    public static boolean isGeocoderAvalibe() {
        return Geocoder.isPresent();
    }

    public static double[] doGeocoding(Context context, String address) throws IOException {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addressList = geocoder.getFromLocationName(address, 1);
        if (addressList != null && addressList.size() > 0) {
            Address result = addressList.get(0);
            return new double[] {result.getLatitude(), result.getLongitude()};
        } else {
            return null;
        }
    }

    public static String doReverseGeocoding(Context context, double latitude, double longitude) throws IOException {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
        if (addressList != null && addressList.size() > 0) {
            Address result = addressList.get(0);

            String fullAddress = "";
            for (int i = 0; i < result.getMaxAddressLineIndex(); i++) {
                String line = result.getAddressLine(i);
                fullAddress += line + "\n";
            }
            return fullAddress.trim();
        } else {
            return null;
        }
    }
}