package com.example.andreas.contextawareudenpeter;

import android.location.Location;

/**
 * Created by Peter on 08-Dec-16.
 */

public class Locator {
    private double accelerometer;
    private Location location;
    private Location busgaden;

    public Locator (double accelerometer, Location location) {
        this.accelerometer = accelerometer;
        this.location = location;
        //busgaden.setLatitude(56.172576); busgaden.setLongitude(10.189234);

    }

    public double getAccelerometerValue() {
        return accelerometer;
    }

    public double getDistanceToNearestBusStop() {
        return location.distanceTo(busgaden);
    }

}
