package com.example.andreas.contextawareudenpeter;

import android.location.Location;

import java.util.List;

/**
 * Created by Peter on 08-Dec-16.
 */

public class Locator {
    private double accelerometer;
    private Location location;
    private CsvReader reader;
    private List<BusStop> busstops;

    public Locator (double accelerometer, Location location) {
        this.accelerometer = accelerometer;
        this.location = location;
        //busstops = reader.getBusStops;

    }

    public double getAccelerometerValue() {
        return accelerometer;
    }

    public BusStopDistance getDistanceToNearestStop() {

        //Find the closest stop and the distance to the user
        BusStop closestStop = busstops.get(0);
        double closestStopDistance = location.distanceTo(closestStop.getLocation());

        for (BusStop busStop : busstops) {
            double distanceToBusStop = location.distanceTo(busStop.getLocation());
            if (closestStopDistance > distanceToBusStop) {
                closestStop = busStop;
                closestStopDistance = distanceToBusStop;
            }
        }

        //Save the distance and name of the busstop
        return new BusStopDistance(closestStop.getName(), closestStopDistance);

    }

}
