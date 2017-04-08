package com.ademir.mapsapp.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ademir.mapsapp.GeocoderHelper;
import com.ademir.mapsapp.R;
import com.ademir.mapsapp.models.Place;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class CurrentLocationActivity extends AppCompatActivity implements OnMapReadyCallback,
                                                            GoogleApiClient.ConnectionCallbacks,
                                                            GoogleApiClient.OnConnectionFailedListener {


    public static final int DEFAULT_ZOOM = 14;

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;

    private Location mLastKnownLocation;

    private boolean mLocationPermissionGranted;

    private ProgressDialog mProgressDialog;

    private EditText etSearch;

    private Button btnSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_location);
        etSearch = (EditText) findViewById(R.id.et_search);
        btnSearch = (Button) findViewById(R.id.btn_search);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchPlace(etSearch.getText().toString());
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromInputMethod(etSearch.getWindowToken(), 0);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, this) //FragmentActivity, OnConnectionFailedListener
                    .addConnectionCallbacks(this)
                    .addApi(LocationServices.API) //The main entry point for location services integration.
                    .addApi(Places.PLACE_DETECTION_API) //provides quick access to the device's current place...
                    .addApi(Places.GEO_DATA_API) //provides access to Google's database of local place and business information...
                    .build();
        }

        if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.current_location_map, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_more_info:
                showCurrentPlace();
                return true;

            case R.id.action_show_search:
                if (etSearch.getVisibility() == View.VISIBLE) {
                    etSearch.setVisibility(View.GONE);
                    btnSearch.setVisibility(View.GONE);
                } else {
                    etSearch.setVisibility(View.VISIBLE);
                    btnSearch.setVisibility(View.VISIBLE);
                }
                return true;

            default:
                return false;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        updateLocationUI();
        getDeviceLocation();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.current_location_map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private void updateLocationUI() {
        if (mMap == null) return;
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
    }

    private void getDeviceLocation() {

        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
            mLastKnownLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);
        }

        // Set the map's camera position to the current location of the device.
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(mLastKnownLocation.getLatitude(),
                        mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));


    }

    private void showCurrentPlace() {
        if (mMap == null) {
            return;
        }

        showProgressDialog(true);

        if (mLocationPermissionGranted) {

            @SuppressWarnings("MissingPermission")
            final PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, null);
            result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                @Override
                public void onResult(@NonNull PlaceLikelihoodBuffer likelyPlaces) {

                    List<Place> places = new ArrayList<>();

                    for (PlaceLikelihood placeLikelihood : likelyPlaces) {

                        String name = (String) placeLikelihood.getPlace().getName();
                        String address = (String) placeLikelihood.getPlace().getAddress();
                        LatLng latLng = placeLikelihood.getPlace().getLatLng();
                        String attributions = (String) placeLikelihood.getPlace().getAttributions();

                        places.add(new Place(name, address, attributions, latLng));
                    }
                    // Release the place likelihood buffer, to avoid memory leaks.
                    likelyPlaces.release();

                    // Show a dialog offering the user the list of likely places, and add a
                    // marker at the selected place.
                    showProgressDialog(false);
                    openPlacesDialog(places);
                }
            });
        } else {
            Toast.makeText(this, "Location unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    private void openPlacesDialog(final List<Place> places) {

        DialogInterface.OnClickListener listener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Place place = places.get(which);
                        LatLng markerLatLng = place.latLng;
                        String markerSnippet = place.address;
                        if (place.attributions != null) {
                            markerSnippet = markerSnippet + "\n" + place.attributions;
                        }

                        mMap.clear();
                        mMap.addMarker(new MarkerOptions()
                                .title(place.name)
                                .position(markerLatLng)
                                .snippet(markerSnippet));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng, DEFAULT_ZOOM + 4));
                    }
                };

        String[] placeNames = new String[places.size()];
        for (int i = 0; i < placeNames.length; i++) {
            placeNames[i] = places.get(i).name;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.text_nearby_places)
                .setItems(placeNames, listener)
                .show();
    }

    private void showProgressDialog(boolean show) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setTitle(getString(R.string.text_loading));
        }
        if (show) {
            mProgressDialog.show();
        } else {
            mProgressDialog.dismiss();
        }
    }

    private void searchPlace(String search) {
        if (search.isEmpty()) return;
        new GeocodingTask().execute(search);
    }

    private class GeocodingTask extends AsyncTask<String, View, double[]> {

        @Override
        protected double[] doInBackground(String... params) {
            showProgressDialog(true);
            try {
                return GeocoderHelper.doGeocoding(CurrentLocationActivity.this, params[0]);
            } catch (final IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(double[] doubles) {
            showProgressDialog(false);
            LatLng latLng = new LatLng(doubles[0], doubles[1]);
            mMap.addMarker(new MarkerOptions()
                            .position(latLng))
                            .setTitle(etSearch.getText().toString());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
        }
    }

}
