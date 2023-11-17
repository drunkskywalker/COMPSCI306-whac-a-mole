// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.sim2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;

import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.sql.Time;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


/**
 * An activity that displays a Google map with a marker (pin) to indicate a particular location.
 */


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, SensorEventListener, GoogleMap.OnMapLongClickListener {
    PolylineOptions polylineOptions;
    static GoogleMap gMap;
    private FusedLocationProviderClient flc1, flc2;
    public LatLng loc;
    public double speed;
    public volatile boolean stop = true;
    public SensorManager sManager;

    public static Handler mHandler;
    public Sensor mSensor;
    public MediaPlayer MP_1;
    public MediaPlayer MP_2;
    Thread mThread;
    Thread lThread;
    Thread tempThread;
    public LatLng[] Record_loc = new LatLng[2];
    public double temperature;

    public ArrayList<Double> temps = new ArrayList<>();

    public boolean temp_rec = false;
    public boolean flag = false;
    LocationCallback locationCallback;
    LocationRequest locationRequest;

    TextView t_4;
    TextView t_3;
    TextView t_2;
    TextView t_1;

    double total_dist = 0;
    double run_dist = 0;
    double walk_dist = 0;
    double run_time = 0;
    double walk_time = 0;
    double total_time = 0;

    Message msg;

    //https://stackoverflow.com/questions/8832071/how-can-i-get-the-distance-between-two-point-by-latlng

    public float distance(LatLng a, LatLng b) {

        double lat_a = a.latitude;
        double lat_b = b.latitude;
        double lng_a = a.longitude;
        double lng_b = b.longitude;
        double earthRadius = 3958.75;
        double latDiff = Math.toRadians(lat_b - lat_a);
        double lngDiff = Math.toRadians(lng_b - lng_a);
        double x = Math.sin(latDiff / 2) * Math.sin(latDiff / 2) +
                Math.cos(Math.toRadians(lat_a)) * Math.cos(Math.toRadians(lat_b)) *
                        Math.sin(lngDiff / 2) * Math.sin(lngDiff / 2);
        double c = 2 * Math.atan2(Math.sqrt(x), Math.sqrt(1 - x));
        double distance = earthRadius * c;

        int meterConversion = 1609;

        return new Float(distance * meterConversion).floatValue();
    }

    private String Coordinate(Location location) {

        double loc_lat = (location.getLatitude());
        double loc_lng = (location.getLongitude());
        String la;
        la = "S";
        if (loc_lat > 0) {
            la = "N";
        } else if (loc_lat == 0) {
            la = "";
        }

        String ln;
        ln = "W";
        if (loc_lng > 0) {
            ln = "E";
        } else if (loc_lng == 0) {
            ln = "";
        }

        return (loc_lat + la + ", " + loc_lng + ln);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        return;
    }


    private void playBG() {

        t_4 = (TextView) findViewById(R.id.music);

        if (MP_1 != null) {
            MP_1.release();

        }

        MP_1 = MediaPlayer.create(MapsActivity.this, R.raw.m1);
        MP_2 = MediaPlayer.create(MapsActivity.this, R.raw.m2);

        MP_1.start();
        MP_1.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer MP_1) {
                try {
                    mThread.sleep(10000);
                    playBG();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        MP_2.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                try {
                    mThread.sleep(10000);
                    playBG();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        });

        if (MP_1.isPlaying()) {
            t_4.setText("Now playing: Melancholic Road");
        }
        else if (MP_2.isPlaying()) {
            t_4.setText("Now playing: Rapid Milky Way");
        }

    }

    private void update(float dist) {

        t_4 = (TextView) findViewById(R.id.music);
        t_3 = (TextView) findViewById(R.id.CoorChange);

        boolean is_running = false;

        if (dist > 2) {
            MP_1.pause();
            MP_2.start();
            is_running = true;

            if (flag) {
                run_time += 1;
                run_dist += dist;
            }
        } else if (dist <= 2) {

            MP_2.pause();
            MP_1.start();
            is_running = false;

            if (flag) {
                walk_dist += dist;
                walk_time += 1;
            }
        }

        if (flag) {
            total_dist += dist;
            total_time += 1;
        }

        t_3.setText("Moved distance in past 1 second: " + dist + ", Total distance: " + total_dist
                + "\nIs running: " + is_running);

        if (MP_1.isPlaying()) {
            t_4.setText("Now playing: Melancholic Road");
        }
        else if (MP_2.isPlaying()) {
            t_4.setText("Now playing: Rapid Milky Way");
        }
    }


    public void update_map() {

        gMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
        polylineOptions.add(loc);
        gMap.addPolyline(polylineOptions);
        //gMap.addMarker(new MarkerOptions().draggable(true).position(loc));

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        sManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mSensor = sManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        t_1 = (TextView) findViewById(R.id.trueloc);
        t_3 = (TextView) findViewById(R.id.CoorChange);

        t_1.setText("Waiting...");

        flc1 = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        flc1.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {

            @Override
            public void onSuccess(Location location) {
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    loc = new LatLng(location.getLatitude(), location.getLongitude());
                    t_1.setText("U R @: \n" + Coordinate(location));

                } else {
                    loc = new LatLng(36, 120);
                }
                Record_loc[0] = loc;
                Record_loc[1] = loc;

                polylineOptions.add(loc);
                //gMap.addPolyline(polylineOptions);
                gMap.moveCamera(CameraUpdateFactory.newLatLng(loc));

            }
        });

/**
        mapThread = new Thread(new Runnable() {

            @Override
            public void run() {

            }

        });
**/


        lThread = new Thread(new Runnable() {

            @Override

            public void run() {

                flc2 = LocationServices.getFusedLocationProviderClient(MapsActivity.this);
                locationRequest = LocationRequest.create();
                locationRequest.setInterval(1000);
                locationRequest.setFastestInterval(1000);
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                locationCallback = new LocationCallback() {

                    @Override
                    public void onLocationResult(LocationResult locationResult) {

                        if (locationResult == null) {
                            return;
                        }
                        for (Location location: locationResult.getLocations()) {

                            loc = new LatLng(location.getLatitude(), location.getLongitude());

                            t_1.setText("U R @: \n" + Coordinate(location));

                            Record_loc[0] = Record_loc[1];
                            Record_loc[1] = loc;

                            float dist = distance(Record_loc[0], Record_loc[1]);

                            update(dist);
                            update_map();

                            if (loc == null || Record_loc[0] == Record_loc[1]) {
                                t_3.setText("Total distance: " + total_dist);
                            }
                        }
                    }
                };

                if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                flc2.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

            }
        });

        tempThread = new Thread(new Runnable() {

            @Override
            public void run() {

                t_2 = (TextView) findViewById(R.id.Step);
                t_2.setText("Step count: " + temperature);
                temps.add(temperature);

            }

        });

    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    public void play(View v) throws IOException {

        Button play = (Button) findViewById(R.id.play);

        Thread fileThread = new Thread(new Runnable() {

            @Override
            public void run() {

                gMap.addMarker(new MarkerOptions().position(loc).title("Record"));

                File filedir = new File(Environment.getExternalStorageDirectory().getPath());
                if (!filedir.exists()) {
                    filedir.mkdirs();
                }
                // Toast.makeText(MapsActivity.this, (String) (Environment.getExternalStorageDirectory().getAbsolutePath()), Toast.LENGTH_LONG).show();

                File file = new File(Environment.getExternalStorageDirectory().getPath(), "info.txt");
                if (!file.exists()) {
                    try {
                        boolean newFile = file.createNewFile();
                    } catch (IOException e) {
                        Toast.makeText(MapsActivity.this, "error - 0", Toast.LENGTH_LONG).show();
                    }
                }
                else {
                    file.delete();
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                try{

                    StringBuilder strB = new StringBuilder();
                    strB.append("Exercise_time,").append(total_time).append("s, \nExercise_distance,").append(total_dist);
                    strB.append("m,\nRunning_time,").append(run_time).append("s,\nRunning_distance,").append(run_dist);
                    strB.append("m,\nTotal_steps,").append(temperature).append(",");

                    FileOutputStream os = new FileOutputStream(file, true);
                    os.write(strB.toString().getBytes());
                    os.flush();
                    os.close();

                    Toast.makeText(MapsActivity.this, "file created", Toast.LENGTH_SHORT).show();
                    Toast.makeText(MapsActivity.this, strB.toString(), Toast.LENGTH_LONG).show();

                } catch (FileNotFoundException e) {
                    Toast.makeText(MapsActivity.this, "error: file not found", Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    Toast.makeText(MapsActivity.this, "error: IOException", Toast.LENGTH_LONG).show();
                }
            }
        });
        if (flag) {

            /**
            File filedir = new File(Environment.getExternalStorageDirectory().getPath());
            if (!filedir.exists()) {
                filedir.mkdirs();
            }
           // Toast.makeText(MapsActivity.this, (String) (Environment.getExternalStorageDirectory().getAbsolutePath()), Toast.LENGTH_LONG).show();

            File file = new File(Environment.getExternalStorageDirectory().getPath(), "info.txt");
            if (!file.exists()) {
                try {
                    boolean newFile = file.createNewFile();
                } catch (IOException e) {
                    Toast.makeText(MapsActivity.this, "error - 0", Toast.LENGTH_LONG).show();
                }
            }
            else {
                file.delete();
                file.createNewFile();
            }

            try{

                StringBuilder strB = new StringBuilder();
                strB.append("Exercise_time,").append(total_time).append("s, \nExercise_distance,").append(total_dist);
                strB.append("m,\nRunning_time,").append(run_time).append("s,\nRunning_distance,").append(run_dist);
                strB.append("m,\nTotal_steps,").append(temperature).append(",");

                FileOutputStream os = new FileOutputStream(file, true);
                os.write(strB.toString().getBytes());
                os.flush();
                os.close();

                Toast.makeText(MapsActivity.this, "file created", Toast.LENGTH_SHORT).show();
                Toast.makeText(MapsActivity.this, strB.toString(), Toast.LENGTH_LONG).show();

            } catch (FileNotFoundException e) {
                Toast.makeText(MapsActivity.this, "error: file not found", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                Toast.makeText(MapsActivity.this, "error: IOException", Toast.LENGTH_LONG).show();
            }
             **/

            fileThread.run();
        }

        if (MP_1 == null) {

            mThread = new Thread (new Runnable() {
                @Override
                public void run() {
                    playBG();
                }
            });

            mThread.run();

        }

        if (!flag) {

            lThread.run();
            // mapThread.run();
            play.setText("Record");

            gMap.addMarker(new MarkerOptions().position(loc).title("Start Point"));
            flag = true;

        }
        flag = true;
    }

    /**
    public void change(View v) {

        if (MP_1.isPlaying() && !MP_2.isPlaying()) {

            MP_1.pause();
            MP_2.start();
        }
        else if (MP_2.isPlaying() && !MP_1.isPlaying()) {

            MP_2.pause();
            MP_1.start();
        }
    }
     **/


    /**
    public void pause(View v){

        Button pause = (Button) findViewById(R.id.pause);

        if (MP_2.isPlaying()) {
            if (!stop) {
                MP_2.pause();
                stop = true;
            }
            else {
                MP_2.start();
                stop = false;
            }
        }
        else if (MP_1.isPlaying()) {
            if (!stop) {
                MP_1.pause();
                stop = true;
            }
            else {
                MP_1.start();
                stop = false;
            }
        }

        else if (!MP_1.isPlaying() && !MP_2.isPlaying()) {
            MP_1.start();
            stop = false;
        }

        if (stop) {
            pause.setText("continue");
        }
        else {
            pause.setText("pause");
        }

    }
**/


// Once stopped, start with the slow one.

    /**
    public void pause (View v) {

        Button pause = (Button) findViewById(R.id.pause);

        if (!stop) {
            if (MP_1.isPlaying()) {
                MP_1.pause();
                stop = true;
                pause.setText("continue");

            } else if (MP_2.isPlaying()) {
                MP_2.pause();
                stop = true;
                pause.setText("continue");
            }

            lThread.interrupt();
            mThread.interrupt();

        }


        else {

            mThread.run();
            MP_1.start();
            stop = false;
            pause.setText("pause");
            lThread.run();

        }
    }

     **/

    @Override
    public void onSensorChanged(SensorEvent e) {

        temp_rec = true;
        temperature = e.values[0];
        tempThread.run();

    }

    @Override
    protected void onResume() {
        super.onResume();
        sManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {

        super.onPause();
        sManager.unregisterListener(this);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {

        gMap = googleMap;
        //LatLng Qingdao = new LatLng(36, 120);
        //gMap = googleMap;
        /**
        Marker Qd = gMap.addMarker(new MarkerOptions()
                .position(Qingdao)
                .draggable(true)
                .title("Qingdao"));

        gMap.moveCamera(CameraUpdateFactory.newLatLng(Qingdao));

        Qd.setTag(0);
         **/
        polylineOptions = new PolylineOptions();
        gMap.setOnMapLongClickListener(this);
        gMap.setMinZoomPreference(15);


    }

    @Override
    public void onMapLongClick(LatLng point) {

        Marker marker = gMap.addMarker(new MarkerOptions()
                .position(point)
                .draggable(true)
                .title("Target"));

        marker.setTag(0);
    }
}

