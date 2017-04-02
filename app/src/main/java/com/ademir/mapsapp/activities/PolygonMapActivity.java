package com.ademir.mapsapp.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.ademir.mapsapp.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PolygonMapActivity extends AppCompatActivity implements OnMapReadyCallback,
                                                                GoogleMap.OnMapClickListener,
                                                                GoogleMap.OnPolylineClickListener,
                                                                GoogleMap.OnPolygonClickListener {

    private static final int COLOR_BLACK_ARGB = 0xff000000;
    private static final int COLOR_WHITE_ARGB = 0xffffffff;
    private static final int COLOR_GREEN_ARGB = 0xff388E3C;
    private static final int COLOR_PURPLE_ARGB = 0xff81C784;
    private static final int COLOR_ORANGE_ARGB = 0xffF57F17;
    private static final int COLOR_BLUE_ARGB = 0xffF9A825;

    private static final int[] colors = new int[] {
            COLOR_BLACK_ARGB, COLOR_WHITE_ARGB, COLOR_GREEN_ARGB,
            COLOR_PURPLE_ARGB, COLOR_ORANGE_ARGB, COLOR_BLUE_ARGB
    };

    private static final int PATTERN_DASH_LENGTH_PX = 20;
    private static final int PATTERN_GAP_LENGTH_PX = 20;

    private static final PatternItem DOT = new Dot();
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);
    private static final PatternItem DASH = new Dash(PATTERN_DASH_LENGTH_PX);

    private static final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DOT);
    private static final List<PatternItem> PATTERN_POLYGON_ALPHA = Arrays.asList(GAP, DASH);
    private static final List<PatternItem> PATTERN_POLYGON_BETA = Arrays.asList(DOT, GAP, DASH, GAP);

    public static final int POLYLINE_MODE = R.id.app_bar_check_polyline;
    public static final int POLYGON_MODE = R.id.app_bar_check_polygon;

    private GoogleMap mMap;

    private int mMode = POLYLINE_MODE;

    private List<LatLng> mPositions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_polygon_map);

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.polygon_map);

        mapFragment.getMapAsync(this);

        mPositions = new ArrayList<>();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.polygon_map, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mMode == POLYLINE_MODE) {
            menu.findItem(R.id.app_bar_check_polygon).setChecked(false);
        } else {
            menu.findItem(R.id.app_bar_check_polyline).setChecked(false);
        }

        menu.findItem(mMode).setChecked(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        item.setChecked(!item.isChecked());

        switch (item.getItemId()) {
            case R.id.app_bar_clear:
                mMap.clear();
                mPositions.clear();
                return true;

            case R.id.app_bar_check_polyline:
                if (item.isChecked()) {
                    mMode = POLYLINE_MODE;
                }
                return true;

            case R.id.app_bar_check_polygon:
                if (item.isChecked()) {
                    mMode = POLYGON_MODE;
                }
                return true;

            default:
                return false;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this);
        mMap.setOnPolylineClickListener(this);
        mMap.setOnPolygonClickListener(this);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        mMap.clear();

        mPositions.add(latLng);

        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Marker " + (mPositions.size() + 1)));

        if (mMode == POLYLINE_MODE) {
            mMap.addPolyline(new PolylineOptions().clickable(true).addAll(mPositions));
        }
        else if (mMode == POLYGON_MODE) {
            mMap.addPolygon(new PolygonOptions().clickable(true).addAll(mPositions));
        }
    }

    @Override
    public void onPolylineClick(Polyline polyline) {
        if ((polyline.getPattern() == null) || (!polyline.getPattern().contains(DOT))) {
            polyline.setPattern(PATTERN_POLYLINE_DOTTED);
        } else {
            polyline.setPattern(null);
        }
    }

    @Override
    public void onPolygonClick(Polygon polygon) {
        int pointCount = polygon.getPoints().size();
        int color = colors[ pointCount % colors.length];

        if (pointCount % 2 == 0) {
            polygon.setStrokePattern(PATTERN_POLYGON_ALPHA);
        } else {
            polygon.setStrokePattern(PATTERN_POLYGON_BETA);
        }

        polygon.setStrokeColor(color);
        polygon.setFillColor(colors[ (pointCount + 1) % colors.length]);
    }

}
