package com.example.andreas.contextawareudenpeter;

/**
 * Created by Peter on 14-Dec-16.
 */

public class BusStopDistance {
    private String name;
    private double distance;

    public BusStopDistance(String name, double distance) {
        this.name = name;
        this.distance = distance;
    }

    public String getName() { return name; }
    public double getDistance() { return distance; }
}
