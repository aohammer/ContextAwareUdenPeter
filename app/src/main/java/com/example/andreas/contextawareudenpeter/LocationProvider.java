package com.example.andreas.contextawareudenpeter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import static android.support.v4.app.ActivityCompat.requestPermissions;


public class LocationProvider {
    // Acquire a reference to the system LocationProvider Manager
    Context locContext;
    LocationManager locationManager;
    Activity activity;
    double longitude;
    double latitude;
    LocationListener listener;
    Location location;

    public LocationProvider(Context locContext) {
        this.locContext = locContext;
        locationManager = (LocationManager) locContext.getSystemService(Context.LOCATION_SERVICE);
        activity = (Activity) locContext;

        listener = new LocationListener() {

            @Override
            public void onLocationChanged(Location loc) {
                location = loc;
                longitude = location.getLongitude();
                latitude = location.getLatitude();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (ActivityCompat.checkSelfPermission(locContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(locContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


            requestPermissions(activity, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.INTERNET
            }, 10);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
    }

    public double getLatitude() {return latitude;}

    public double getLongitude() {return longitude;}

    public Location getLocation(){
        return location;
    }

    public void stopListener() {
        if (ActivityCompat.checkSelfPermission(locContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(locContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(activity, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.INTERNET
            }, 10);
            return;
        }
        locationManager.removeUpdates(listener);
    }
}
