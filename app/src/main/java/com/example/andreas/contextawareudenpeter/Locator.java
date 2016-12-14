package com.example.andreas.contextawareudenpeter;

import android.location.Location;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Peter on 08-Dec-16.
 */

public class Locator {
    private double accelerometer;
    private Location location;
    private Location busgaden;
    private String csvFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/location/bustimes.csv";

    private BufferedReader br = null;
    private String line = "";
    private String splitString = ",";



    public Locator (double accelerometer, Location location) {
        this.accelerometer = accelerometer;
        this.location = location;
        try {
            csvReader();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double getAccelerometerValue() {
        return accelerometer;
    }

    public double getDistanceToNearestBusStop() {
        return location.distanceTo(busgaden);
    }

    public void csvReader() throws IOException {
        br = new BufferedReader(new FileReader(csvFile));
        while ((line = br.readLine()) != null){
            String[] busStops = line.split(splitString);
        }
    }



}
