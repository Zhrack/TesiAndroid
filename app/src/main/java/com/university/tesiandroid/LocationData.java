package com.university.tesiandroid;

import android.location.Location;

import java.util.ArrayList;

/**
 * Created by Davide on 07/04/2016.
 */
public class LocationData {

    private ArrayList<PointInfo> points = new ArrayList<>();
    private Location mLastLocation;
    private double latitude;
    private double longitude;

    private static LocationData instance;

    private LocationData()
    {
        instance = null;
    }

    public static LocationData Instance()
    {
        if(instance == null)
        {
            instance = new LocationData();
        }

        return instance;
    }

    public ArrayList<PointInfo> getPoints() {
        return points;
    }

    public void setPoints(ArrayList<PointInfo> points) {
        this.points = points;
    }

    public Location getmLastLocation() {
        return mLastLocation;
    }

    public void setmLastLocation(Location mLastLocation) {
        this.mLastLocation = mLastLocation;

        this.latitude = this.mLastLocation.getLatitude();
        this.longitude = this.mLastLocation.getLongitude();
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
