package com.example.andreas.contextawareudenpeter;

import android.location.Location;

/**
 * Created by Peter on 08-Dec-16.
 */

public class Locator {
    private double accelerometer;
    private Location location;



    public Locator (double accelerometer, Location location) {
        this.accelerometer = accelerometer;
        this.location = location;
    }

    public double getAccelerometerValue() {
        return accelerometer;
    }

    public Location getLocation() {
        return location;
    }

}
