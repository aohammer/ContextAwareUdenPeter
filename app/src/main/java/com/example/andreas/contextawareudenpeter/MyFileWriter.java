package com.example.andreas.contextawareudenpeter;

/**
 * Created by Peter on 27-Nov-16.
 */

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.TimeZone;

public class MyFileWriter {
    String fileName;
    String root;
    String path;
    List<Double> windows = new ArrayList<>();

    public void writeToFile(String filename, String data){

        try {
            root = Environment.getExternalStorageDirectory().getAbsolutePath();
            path = "/location";
            File myDir = new File(root + path);
            myDir.mkdirs();

            File file = new File(myDir, filename);

            if (file.exists()) {
                file.delete();
            }

            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(data.getBytes());
            fileOutputStream.flush();
            fileOutputStream.close();

            Log.i("System.out", file.getAbsolutePath());
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
}
