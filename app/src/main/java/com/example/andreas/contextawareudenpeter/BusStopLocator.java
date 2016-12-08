package com.example.andreas.contextawareudenpeter;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationProvider;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Peter on 08-Dec-16.
 */

public class BusStopLocator implements SensorEventListener {

    private LocationProvider locationProvider;
    private SensorManager sensorManager;
    private Sensor senAccelerometer;
    private List<Double> samples = new ArrayList<>();
    private List<Double> samplesOverlap = new ArrayList<>();
    private int counter = 0;
    MyFileWriter fw;
    String data = "";

    public BusStopLocator(Context context) {
        //sensor setup
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

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
                samples.add(accData);
                Log.d("Counter",counter + "");
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
        data = "";
    }

    //Help-methods
    private void calculateValues(List<Double> samples) {
        double min = samples.get(0);
        double max = samples.get(0);
        double avg;
        double sd = 0;
        double sum = 0;

        for (Double sample : samples) {
            sum += sample;
            if(sample < min) min = sample;
            if(sample > max) max = sample;
        }
        avg = sum / samples.size();

        sd = standardDeviation(avg, samples);

        Log.d("Window Values", "Min: " + min + " - Max: " + max + " - Avg: " + avg + " - Sd: " + sd);
        data += min + ";" + max + ";" + sd + "\n";
    }

    private double standardDeviation(double avg, List<Double> samples) {
        double sd = 0;

        for (Double sample : samples)
        {
            sd = sd + Math.pow(sample - avg, 2);
        }

        sd = Math.sqrt(sd/samples.size());

        return sd;
    }
}
