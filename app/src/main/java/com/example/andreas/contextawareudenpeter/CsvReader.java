package com.example.andreas.contextawareudenpeter;

import android.location.Location;
import android.os.Environment;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anders on 14-12-2016.
 */

public class CsvReader {
    private String csvFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/location/bustimes.csv";
    private BufferedReader br = null;
    private String line = "";
    private String splitString = ",";
    private List<BusStop> bustops = new ArrayList<>();


    public CsvReader(){
            read();
    }

    public void read(){

        try {
            br = new BufferedReader(new FileReader(csvFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {

            while ((line = br.readLine()) != null){
                String[] csvFile = line.split(splitString);

                //Getting busstops names
                String busStopName = csvFile[0];

                //Getting long and lat from array and convert them to doubles.
                String longitude = csvFile[2];
                String latitute = csvFile[1];
                double Dlongitude = Double.parseDouble(longitude);
                double Dlatitude = Double.parseDouble(latitute);

                //creating a location and set them to the long and lat
                Location location = new Location("");
                location.setLongitude(Dlongitude);
                location.setLatitude(Dlatitude);

                //Getting time from the array and converting them to integers and add them to a list
                String time1 = csvFile[3];
                String time2 = csvFile[4];

                int firstTime = Integer.parseInt(time1);
                int secondTime = Integer.parseInt(time2);

                List<Integer> schedual = new ArrayList<>();

                schedual.add(firstTime);
                schedual.add(secondTime);

                //Adding busstops to arraylist
                bustops.add(new BusStop(busStopName, location, schedual));

            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public List<BusStop> getBusstops(){
        return bustops;
    }
}
