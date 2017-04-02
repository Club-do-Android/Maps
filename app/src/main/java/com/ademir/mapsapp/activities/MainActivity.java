package com.ademir.mapsapp.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.ademir.mapsapp.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int PERMISSIONS_REQUEST_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_marker_map).setOnClickListener(this);
        findViewById(R.id.btn_polygon_map).setOnClickListener(this);
        findViewById(R.id.btn_current_location_map).setOnClickListener(this);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivity(new Intent(MainActivity.this, CurrentLocationActivity.class));
                }
            }
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.btn_marker_map:
                startActivity(new Intent(this, MarkerMapActivity.class));
                break;

            case R.id.btn_polygon_map:
                startActivity(new Intent(this, PolygonMapActivity.class));
                break;

            case R.id.btn_current_location_map:
                if (requestLocationPermission()) {
                    startActivity(new Intent(this, CurrentLocationActivity.class));
                }
                break;

            default:
                break;

        }

    }

    private boolean checkPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission(int requestCode, String... permissions) {
        ActivityCompat.requestPermissions(this, permissions, requestCode);
    }

    private boolean requestLocationPermission() {
        if (!checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            requestPermission(PERMISSIONS_REQUEST_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION);
            return false;
        }
        return true;
    }
}
