package com.example.andreas.contextawareudenpeter;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
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
    private Instance instance;
    private List<Locator> samples = new ArrayList<>();
    private List<Locator> samplesOverlap = new ArrayList<>();
    private int counter = 0;
    private Context context;
    MyFileWriter fw;
    String data = "MIN, MAX, SD, BUS STOP, MIN DISTANCE, MAX DISTANCE, AVG DISTANCE, gt \n";
    private double fDistribution =0;
    private TextView headingText;
    private TextView departureText;
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+1.00"));


    public BusStopLocator(Context context, LocationProvider locationProvider) {
        this.context = context;

        //sensor setup
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.locationProvider = locationProvider;

        //fileManager setup
        fw = new MyFileWriter();

        senAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        this.headingText = (TextView) ((Activity)context).findViewById(R.id.headingText);
        this.departureText = (TextView) ((Activity)context).findViewById(R.id.departureText);
        departureText.setText("Fetching...");

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
            FastVector fastVector = new FastVector(2);

            fastVector.addElement("FALSE");
            fastVector.addElement("TRUE");
            Attribute ClassAttribute = new Attribute("qt", fastVector);

            // Declare the feature vector
            FastVector wekaAttributes = new FastVector(7);
            wekaAttributes.addElement(Attribute1);
            wekaAttributes.addElement(Attribute2);
            wekaAttributes.addElement(Attribute3);
            wekaAttributes.addElement(Attribute4);
            wekaAttributes.addElement(Attribute5);
            wekaAttributes.addElement(Attribute6);
            wekaAttributes.addElement(ClassAttribute);

            // Create empty instance
            Instances trainingSet = new Instances("Rel", wekaAttributes, 7);
            trainingSet.setClassIndex(6);

            //Our instance
            instance = new DenseInstance(trainingSet.numAttributes());
            trainingSet.add(instance);
            instance.setValue((Attribute)wekaAttributes.elementAt(0), min);
            instance.setValue((Attribute)wekaAttributes.elementAt(1), max);
            instance.setValue((Attribute)wekaAttributes.elementAt(2), sd);
            instance.setValue((Attribute)wekaAttributes.elementAt(3), minBsd.getDistance());
            instance.setValue((Attribute)wekaAttributes.elementAt(4), maxBsd);
            instance.setValue((Attribute)wekaAttributes.elementAt(5), avgBsd);
            instance.setMissing(6);
            instance.setDataset(trainingSet);

            // deserialize model
            Classifier cls = (Classifier) weka.core.SerializationHelper.read(Environment.getExternalStorageDirectory().getAbsolutePath() + "/location/busstop.model");
            fDistribution = cls.classifyInstance(instance);
            Log.d("WEKA", fDistribution + "");
            atBusStop(minBsd);

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

    private void addNotification(BusStopDistance bsd) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this.context)
                        .setSmallIcon(R.drawable.busico)
                        .setContentTitle("You are at " + bsd.getName())
                        .setContentText("Click here to view bus details");

        Intent notificationIntent = new Intent(this.context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this.context, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) context.getSystemService(this.context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }

    public boolean atBusStop(BusStopDistance bsd){

        if(fDistribution==1.0){
            addNotification(bsd);
            String nextBusStop = "Invalid";

            Date currentTime = cal.getTime();
            DateFormat date = new SimpleDateFormat("HH:mm");
            DateFormat hour = new SimpleDateFormat("HH");
            date.setTimeZone(TimeZone.getTimeZone("GMT+1:00"));

            String localTime = date.format(currentTime);
            String hourTime = hour.format(currentTime);

            //Get all bus stops to find the next one
            CsvReader cv = new CsvReader();
            List<BusStop> busList= cv.getBusstops();

            int j = 0;
            for (BusStop bus : busList) {
                if (bsd.getName().equals(bus.getName())) {
                    j++;
                    //If last in list get the first
                    if (busList.size() < j) {
                        j = 0;
                    }
                    nextBusStop = busList.get(j).getName();

                    //Take current time and the minute a bus depart to get a depart time
                    String t = hourTime + ":" + bsd.getTime();
                    DateFormat df = new SimpleDateFormat("HH:mm");
                    try {
                        if (currentTime.after(df.parse(t))) {
                            int h = Integer.parseInt(hourTime);
                            h++;
                            hourTime = h + "";
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }

            departureText.setText(hourTime + ":" + bsd.getTime());
            headingText.setText(nextBusStop);
            return true;
        } else{
            departureText.setText("...");
            headingText.setText("...");
            return false;
        }
    }

}
