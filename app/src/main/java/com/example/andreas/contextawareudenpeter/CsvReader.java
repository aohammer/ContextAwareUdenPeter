package com.example.andreas.contextawareudenpeter;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Anders on 14-12-2016.
 */

public class CsvReader {
    private String csvFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/location/bustimes.csv";
    private BufferedReader br = null;
    private String line = "";
    private String splitString = ",";


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
                String[] busStops = line.split(splitString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
