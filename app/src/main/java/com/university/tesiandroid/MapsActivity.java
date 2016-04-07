package com.university.tesiandroid;

import android.graphics.Point;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private LatLngBounds.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        builder = new LatLngBounds.Builder();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                // Set the camera to the greatest possible zoom level that includes the
                // bounds
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50), 2000, null);
            }
        });

        LocationData locationData = LocationData.Instance();
        List<PointInfo> list = locationData.getPoints();

        for(int i = 0; i < list.size(); ++i)
        {
            PointInfo point = list.get(i);
            LatLng location = new LatLng(point.getLatitude(), point.getLongitude());
            mMap.addMarker(
                    new MarkerOptions().
                            position(location).
                            title(point.getName()).snippet("Distance: " + point.getDistance() + "\n" + point.getWikiText())
            );
//            bounds = bounds.including(location);
            builder.include(location);
        }

        LatLng devicePosition = new LatLng(locationData.getLatitude(), locationData.getLongitude());
        mMap.addMarker(
                new MarkerOptions().
                        position(devicePosition).
                        title("Your position")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        );
        builder.include(devicePosition);

    }
}
