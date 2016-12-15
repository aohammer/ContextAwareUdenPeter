package com.example.andreas.contextawareudenpeter;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Environment;
import android.util.Log;

import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Created by Peter on 08-Dec-16.
 */

public class BusStopLocator implements SensorEventListener {

    private LocationProvider locationProvider;
    private SensorManager sensorManager;
    private Sensor senAccelerometer;
    private Instance iUse;
    private Context context;

    private List<Locator> samples = new ArrayList<>();
    private List<Locator> samplesOverlap = new ArrayList<>();
    private int counter = 0;
    MyFileWriter fw;
    String data = "MIN, MAX, SD, BUS STOP, MIN DISTANCE, MAX DISTANCE, AVG DISTANCE, gt \n";

    public BusStopLocator(Context context, LocationProvider locationProvider) {
        this.context = context;
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
        data = "MIN, MAX, SD, BUS STOP, MIN DISTANCE, MAX DISTANCE, AVG DISTANCE, gt \n";
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
        BusStopDistance minBsd = l.getDistanceToNearestStop();

        //Iterate window
        for (Locator sample : samples) {
            double acc = sample.getAccelerometerValue();
            BusStopDistance bsd = sample.getDistanceToNearestStop();

            //Add data to sum to get avg
            sum += acc;

            //Update min and max accelerator data
            if(acc < min) min = acc;
            if(acc > max) max = acc;

            //Find min distance to bus stop
            if(bsd.getDistance() < minBsd.getDistance()) { minBsd = bsd; }
        }

        double maxBsd = l.getDistanceToNearestStop().getDistance();
        double avgBsd;
        double sumBsd = 0;

        //Iterate window again to make sure GPS coordinates is to the right bus stop
        for (Locator sample : samples) {
            BusStopDistance bsd = sample.getDistanceToNearestStop();

            if (bsd.getName().equals(minBsd.getName())) {
                sumBsd += bsd.getDistance();

                if(bsd.getDistance() > maxBsd) { maxBsd = bsd.getDistance(); }
            }
        }

        int size = samples.size();

        //Calculate standard deviation for accelerometer
        avg = sum / size;
        sd = standardDeviation(avg, samples);

        //Calculate average for distance
        avgBsd = sumBsd / size;


        //---- WEKA STUFF ---- //
        try {
            // Declare two numeric attributes
            Attribute Attribute1 = new Attribute("MinAcc");
            Attribute Attribute2 = new Attribute("MaxAcc");
            Attribute Attribute3 = new Attribute("SdAcc");
            Attribute Attribute4 = new Attribute("MinDis");
            Attribute Attribute5 = new Attribute("MaxDis");
            Attribute Attribute6 = new Attribute("AvgDis");

            // Declare the class attribute along with its values
            FastVector fvClassVal = new FastVector(2);
            fvClassVal.addElement("FALSE");
            fvClassVal.addElement("TRUE");
            Attribute ClassAttribute = new Attribute("qt", fvClassVal);

            // Declare the feature vector
            FastVector fvWekaAttributes = new FastVector(7);
            fvWekaAttributes.addElement(Attribute1);
            fvWekaAttributes.addElement(Attribute2);
            fvWekaAttributes.addElement(Attribute3);
            fvWekaAttributes.addElement(Attribute4);
            fvWekaAttributes.addElement(Attribute5);
            fvWekaAttributes.addElement(Attribute6);
            fvWekaAttributes.addElement(ClassAttribute);

            // Create empty instance
            Instances isTrainingSet = new Instances("Rel", fvWekaAttributes, 7);
            isTrainingSet.setClassIndex(6);

            //Our instance
            iUse = new Instance(isTrainingSet.numAttributes());
            isTrainingSet.add(iUse);
            iUse.setValue((Attribute)fvWekaAttributes.elementAt(0), min);
            iUse.setValue((Attribute)fvWekaAttributes.elementAt(1), max);
            iUse.setValue((Attribute)fvWekaAttributes.elementAt(2), sd);
            iUse.setValue((Attribute)fvWekaAttributes.elementAt(3), minBsd.getDistance());
            iUse.setValue((Attribute)fvWekaAttributes.elementAt(4), maxBsd);
            iUse.setValue((Attribute)fvWekaAttributes.elementAt(5), avgBsd);
            iUse.setMissing(6);
            iUse.setDataset(isTrainingSet);

            // deserialize model
            Classifier cls = (Classifier) weka.core.SerializationHelper.read(Environment.getExternalStorageDirectory().getAbsolutePath() + "/location/busstop.model");
            double fDistribution = cls.classifyInstance(iUse);
            Log.d("WEKA", fDistribution + "");

        } catch (Exception e) {
            e.printStackTrace();
        }

        //Log.d("Window Value", "Min: " + min + " - Max: " + max + " - Avg: " + avg + " - Sd: " + sd + " - Distance: " + minBsd.getDistance() + " - Bus Stop: " + minBsd.getName());
        data += min + "," + max + "," + sd +  ", " + minBsd.getName() + ", " + minBsd.getDistance() + ", " + maxBsd + ", " + avgBsd + "\n";
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
