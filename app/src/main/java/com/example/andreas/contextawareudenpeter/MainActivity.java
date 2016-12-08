package com.example.andreas.contextawareudenpeter;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    BusStopLocator bs;
    LocationProvider locationProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        locationProvider = new LocationProvider(this);
    }

    public void startListening() {
        bs = new BusStopLocator(this, locationProvider);
        Toast.makeText(MainActivity.this, "Start",
                Toast.LENGTH_SHORT).show();
    }

    public void stopListening() {
        Toast.makeText(MainActivity.this, "Stop",
                Toast.LENGTH_SHORT).show();
        bs.stopAndWrite();
    }
}
