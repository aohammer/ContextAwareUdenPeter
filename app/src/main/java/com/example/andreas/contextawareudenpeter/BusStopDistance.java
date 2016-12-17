package com.example.andreas.contextawareudenpeter;

/**
 * Created by Peter on 14-Dec-16.
 */

public class BusStopDistance {
    private String name;
    private double distance;
    private int time;

    public BusStopDistance(String name, double distance, int time) {
        this.name = name;
        this.distance = distance;
        this.time = time;
    }

    public String getName() { return name; }
    public double getDistance() { return distance; }
    public int getTime(){ return time; }


}
