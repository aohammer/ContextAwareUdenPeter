package com.example.andreas.contextawareudenpeter;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private List<Double> samples = new ArrayList<>();
    private int counter = 0;
    MyFileWriter fw;
    String data = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //sensor setup
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //fileManager setup
        fw = new MyFileWriter();

        //button setup
        Button startButton = (Button) findViewById(R.id.buttonStart);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startListening();
            }
        });

        Button stopButton = (Button) findViewById(R.id.buttonStop);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopListening();
            }
        });

        senSensorManager.unregisterListener(this, senAccelerometer);
    }

    public void startListening() {
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stopListening() {
        senSensorManager.unregisterListener(this, senAccelerometer);
        fw.writeToFile("data.csv", data);
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
                Log.d("tag",counter + "");
            } else {
                counter = 0;
                Log.d("tag2",counter + " nu beregner vi");
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

                for (Double sample : samples)
                {
                    sd = sd + Math.pow(sample - avg, 2);
                }

                sd = Math.sqrt(sd/samples.size());

                Log.d("hej", "Min: " + min + " - Max: " + max + " - Avg: " + avg + " - Sd: " + sd);
                data += min + ";" + max + ";" + sd + "\n";

                samples.clear();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected void onResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause() {
        super.onPause();
        senSensorManager.unregisterListener(this);

    }
}
