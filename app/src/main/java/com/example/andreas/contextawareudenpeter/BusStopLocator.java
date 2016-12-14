package com.example.andreas.contextawareudenpeter;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Peter on 08-Dec-16.
 */

public class BusStopLocator implements SensorEventListener {

    private LocationProvider locationProvider;
    private SensorManager sensorManager;
    private Sensor senAccelerometer;
    private List<Locator> samples = new ArrayList<>();
    private List<Locator> samplesOverlap = new ArrayList<>();
    private int counter = 0;
    MyFileWriter fw;
    String data = "MIN, MAX, SD, MIN DISTANCE, MAX DISTANCE, AVG DISTANCE, BUS STOP, gt \n";

    public BusStopLocator(Context context, LocationProvider locationProvider) {
        //sensor setup
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.locationProvider = locationProvider;

        //fileManager setup
        fw = new MyFileWriter();

        senAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            //Euclidean norm calculation (x^2+y^2+z^2)^(1/2)
            double accData = Math.sqrt(Math.pow(event.values[0], 2) + Math.pow(event.values[1], 2) + Math.pow(event.values[2], 2));

            //Add to sample window of 128 values

            if(counter < 128){
                counter++;
                Log.d("Counter",counter + "");
                Location loc = locationProvider.getLocation();
                Locator l = new Locator(accData, loc);
                samples.add(l);
            } else {
                counter = 0;
                calculateValues(samples);

                for(int i = 0; i < 64; i++) {
                    samplesOverlap.add(samples.get(i));
                }

                if (samplesOverlap.size() > 64) {
                    calculateValues(samplesOverlap);
                }

                samplesOverlap.clear();

                for(int i = 64; i < 128; i++) {
                    samplesOverlap.add(samples.get(i));
                }

                samples.clear();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void stopAndWrite() {
        sensorManager.unregisterListener(this, senAccelerometer);
        Log.d("data.csv", data);
        fw.writeToFile("data.csv", data);
        data = "MIN, MAX, SD, MIN DISTANCE, MAX DISTANCE, AVG DISTANCE, BUS STOP, gt \n";
    }

    //Help-methods
    private void calculateValues(List<Locator> samples) {
        Locator l = samples.get(0);

        //Accelerometer variables
        double min = l.getAccelerometerValue();
        double max = l.getAccelerometerValue();
        double avg;
        double sd = 0;
        double sum = 0;

        //Location variables
        //Location loc = l.getLocation();
        //double gpsMinLong = loc.getLongitude();
        //double gpsMaxLong = loc.getLongitude();
        //double gpsMinLat = loc.getLatitude();
        //double gpsMaxLat = loc.getLatitude();
        double gpsSumLong = 0;
        double gpsSumLat = 0;
        double gpsAvgLong;
        double gpsAvgLat;

        //Iterate window
        for (Locator sample : samples) {
            double acc = sample.getAccelerometerValue();
            //Location gps = sample.getLocation();

            //double longi = gps.getLongitude();
            //double lati = gps.getLatitude();

            sum += acc;
            //gpsSumLong += longi;
            //gpsSumLat += lati;

            if(acc < min) min = acc;
            if(acc > max) max = acc;

            //if(longi < gpsMinLong) gpsMinLong = longi;
            //if(longi > gpsMaxLong) gpsMaxLong = longi;
            //if(lati < gpsMinLat) gpsMinLat = lati;
            //if(lati > gpsMaxLat) gpsMaxLat = lati;

        }
        avg = sum / samples.size();
        sd = standardDeviation(avg, samples);

        Log.d("Window Value", "Min: " + min + " - Max: " + max + " - Avg: " + avg + " - Sd: " + sd);
        data += min + "," + max + "," + sd +  ", " + /* gpsMin + ", " + gpsMax + ", " + gpsAvg + */ "\n";
    }

    private double standardDeviation(double avg, List<Locator> samples) {
        double sd = 0;

        for (Locator sample : samples)
        {
            double acc = sample.getAccelerometerValue();
            sd = sd + Math.pow(acc - avg, 2);
        }

        sd = Math.sqrt(sd/samples.size());

        return sd;
    }
}
