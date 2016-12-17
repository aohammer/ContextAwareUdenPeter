package com.example.andreas.contextawareudenpeter;

import android.location.Location;

import java.util.List;

/**
 * Created by Peter on 14-Dec-16.
 */

public class BusStop {
    String name;
    Location location;
    int  schedule;

    public BusStop(String name, Location location, int schedule) {
        this.name = name;
        this.location = location;
        this.schedule = schedule;
    }

    public String getName() { return name; }

    public Location getLocation() { return location;}

    public int getSchedule() { return schedule; }

    public String toString() {
        return name + ", " + location.toString() + ", " + schedule;
    }


}
