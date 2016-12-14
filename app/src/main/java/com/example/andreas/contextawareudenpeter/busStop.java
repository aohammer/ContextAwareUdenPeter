package com.example.andreas.contextawareudenpeter;

import android.location.Location;

import java.util.List;

/**
 * Created by Peter on 14-Dec-16.
 */

public class busStop {
    String name;
    Location location;
    List<Integer> schedule;

    public busStop (String name, Location location, List<Integer> schedule) {
        this.name = name;
        this.location = location;
        this.schedule = schedule;
    }

    public String getName() { return name; }

    public Location getLocation() { return location;}

    public List<Integer> getSchedule() { return schedule; }


}
