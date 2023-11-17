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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity
        extends AppCompatActivity
        implements OnMapReadyCallback, OnMarkerClickListener{

    public GoogleMap gMap;
    public UiSettings GUI;

    private SharedPreferences highScore;
    public int hi_perm;
    public boolean sound = true;
    public boolean game_start = false;
    public int score, difficulty = 0;

    public int current_life, this_round;

    // points added to moles hit
    public static int add_score = 100;

    // point addition bonus rate
    public double[] extra_rate = {0.25, 1, 1.25, 1.5};

    // base points taken from moles missed
    public static int sub_score = -50;

    // rate of point deduction if a mole is missed
    public double[] reduce_rate = {0, 0.05, 0.25, 0.1};

    // possibility of a marker to become a mole
    public double[] mole_rate = {0.15, 0.25, 0.3, 0.55};

    // possibility of a *mole* to become mine
    public double[] mine_rate = {0.01, 0.05, 0.1, 0.25};

    // iterations before game end
    public int[] round = {25, 50, 125, 31415926};

    // lives
    public int[] life_remain = {10, 5, 3, 1};

    // bonus for lives remaining
    public double[] life_bonus = {0, 100, 300, 10000000};

    // unit: second
    public int[] round_time = {5000, 3000, 2500, 1500};

    // music box

    //todo: use url access

    public int[] music = {R.raw.e, R.raw.e, R.raw.h, R.raw.h};

    public MediaPlayer MP;
    public MediaPlayer miss;
    public MediaPlayer hit;

    public Thread highscoreThread;
    public Thread musicThread;
    public Thread SEThread;

    public volatile int SEtype;

    public List<Marker> marker_list = new ArrayList<Marker>();

    public TextView game_info, score_board;

    public final String[] items = {"Easy", "Normal", "Hard", "Lunatic"};

    public class gameThread extends AsyncTask<Integer, Integer, Boolean> {

        private TextView game_info;
        private TextView score_board;

        public gameThread(TextView tv, TextView tv1) {

            this.game_info = tv;
            this.score_board = tv1;

        }


        @SuppressLint("SetTextI18n")
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            score = 0;
            this_round = 0;
            current_life = life_remain[difficulty];
            game_info.setText("round:" + (this_round + 1)
                    + "; health:" + current_life
                    + "; difficulty:" + difficulty);
            score_board.setText("High Score: " + hi_perm + "\nYour score:" + score);
        }

        @Override
        protected Boolean doInBackground(Integer... param) {

            int diff = param[0];
            while ((game_start) && (this_round < round[difficulty]) && (current_life > 0)) {

                publishProgress(-1);

                this_round ++;

                try {
                    Thread.sleep(round_time[diff]);
                    publishProgress(-2);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

            game_start = false;
            score += current_life * life_bonus[difficulty];
            return game_start;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {

            if (values[0] == -1){
                assign();
            }


            else if (values[0] == -2) {
                de_assign(game_start);
                game_info.setText("round:" + (this_round) + "; health:" + current_life + "; difficulty:" + items[difficulty]);
                if (score > hi_perm) {

                    score_board.setText("New High Score: " + score);
                }
                else {
                    score_board.setText("High Score: " + hi_perm + "; your score:" + score);
                }
            }
        }

        @Override
        protected void onPostExecute(Boolean re) {

            // Only call score updates when the game is ended (not terminated)

            if (this_round >= round[difficulty] || current_life <= 0){

                if (score > hi_perm) {
                    showHighScoreDialog();
                } else {
                    showScoreDialog();
                }
            }

            de_assign(game_start);
            game_info.setText("Game Info");
            score_board.setText("High Score: " + hi_perm);

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        musicThread.interrupt();
        game_start = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Retrieve the content view that renders the map.

        setContentView(R.layout.activity_maps);

        // Get the SupportMapFragment and request notification when the map is ready to be used.

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        highScore = getSharedPreferences("Score", MODE_PRIVATE);
        hi_perm = highScore.getInt("Highscore", 0);


        highscoreThread = new Thread (new Runnable () {
            @Override
            public void run() {

                hi_perm = score;
                SharedPreferences.Editor e = highScore.edit();
                e.putInt("Highscore", hi_perm);
                e.commit();
                }
                
        });



        musicThread = new Thread(new Runnable() {
            @Override
            public void run() {


                if (sound){
                    if (game_start) {
                        PlayBG(music[difficulty]);
                    } else {
                        PlayBG(R.raw.intro);
                    }
                }
            }
        });

        musicThread.run();

        SEThread = new Thread(new Runnable() {
            @Override
            public void run() {

                if (sound){
                    miss = MediaPlayer.create(MapsActivity.this, R.raw.miss);
                    hit = MediaPlayer.create(MapsActivity.this, R.raw.score);

                    if (SEtype == 1) {

                        miss.start();

                    } else if (SEtype == 0) {

                        hit.start();
                    }
                }

            }
        });

        score_board = (TextView) findViewById(R.id.score);
        score_board.setText("High Score: " + hi_perm);

    }

    public void begin_game(View v) {

        game_start = false;


        score = 0;
        game_start = true;
        musicThread.interrupt();
        musicThread.run();
        game_info = (TextView) findViewById(R.id.info);
        score_board = (TextView) findViewById(R.id.score);
        new gameThread(game_info, score_board).execute(difficulty);

    }

    public void see_high_score(View v) {

        game_start = false;
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(MapsActivity.this);
        normalDialog.setIcon(R.drawable.red_star);
        normalDialog.setTitle("Clear high score?");
        normalDialog.setMessage("Current high score: " + hi_perm + ".\n Are you sure you want to clear the record?");
        normalDialog.setPositiveButton("Affirmative.",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        SharedPreferences.Editor e = highScore.edit();
                        e.putInt("Highscore", 0);
                        e.commit();
                        hi_perm = 0;
                        score_board = (TextView) findViewById(R.id.score);
                        score_board.setText("Record Cleared");
                        Toast.makeText(MapsActivity.this, "Record cleared.", Toast.LENGTH_SHORT).show();
                    }
                });
        normalDialog.setNegativeButton("On second thought...",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });


        normalDialog.show();

    }

    public void see_rule (View v) {

        game_start = false;
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(MapsActivity.this);
        normalDialog.setIcon(R.drawable.q);
        normalDialog.setTitle("Game rules");
        normalDialog.setMessage(
                "Similar to ordinary whac-a-mole game, click on the moles to gain points." +
                        "\nMissing a mole will result in score reduction." +
                        "\nAvoid clicking on mines. This will result in life loss." +
                        "\nMarkers become transparent and eventually invisible as a penalty of clicking on mines."

        );
        normalDialog.setPositiveButton("Received.",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //SharedPreferences.Editor ed = highScore.edit();
                        //ed.putInt("Highscore", hi_temp);
                    }
                });
        normalDialog.setNegativeButton("Onwards!",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        normalDialog.show();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        LatLng zero = new LatLng(0, 0);

        try{
            Marker M = gMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.sound))
                    .position(new LatLng(-60, 36)));
            M.setTag((int[]) new int[]{0, 0, 22});


        } catch (Exception e) {
            Toast.makeText(MapsActivity.this, "Error Making marker", Toast.LENGTH_SHORT).show();
        }

        create_marker();

        LatLngBounds bound = new LatLngBounds(zero, zero);

        PolygonOptions pgo = new PolygonOptions()
                .add(new LatLng(-60, -60))
                .add(new LatLng(-60, 60))
                .add(new LatLng(60, 60))
                .add(new LatLng(60, -60));
        gMap.addPolygon(pgo);

        gMap.moveCamera(CameraUpdateFactory.newLatLng(zero));

        Marker mvd = gMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.q))
                .title("VOID")
                .snippet("-What's in here?\n-You will find out - next round.")
                .position(new LatLng(-60, -36)));
        mvd.setTag((int[]) new int[]{0, 2, 21});

        Marker mmo = gMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.mole))
                .title("MOLE")
                .snippet("Provides you with points")
                .position(new LatLng(-60, -12)));
        mmo.setTag((int[]) new int[]{0, 0, 21});

        Marker mmi = gMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.mine))
                .title("MINE")
                .snippet("Will explode")
                .position(new LatLng(-60, 12)));
        mmi.setTag((int[]) new int[]{0, 1, 21});

        // fix scrolling range, disable zooming and rotating

        gMap.setLatLngBoundsForCameraTarget(bound);

        GUI = gMap.getUiSettings();
        GUI.setZoomControlsEnabled(false);
        GUI.setZoomGesturesEnabled(false);
        GUI.setRotateGesturesEnabled(false);
        gMap.setOnMarkerClickListener(this);

    }

    public void change_difficulty(View v) {
        game_start = false;
        showDifficultyDialog();
    }

    public void PlayBG(int m) {

        if (MP != null) {
            MP.release();
        }

        MP = MediaPlayer.create(MapsActivity.this, m);
        MP.start();

        MP.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer MP) {
                try {
                    musicThread.sleep(1000);

                    if (game_start){
                        PlayBG(music[difficulty]);
                    }
                    else {
                        PlayBG(R.raw.intro);
                    }
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });


    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {

        int[] status = (int[]) marker.getTag();

        if (status[2] == 22){

            if (sound){
                sound = false;
                MP.pause();
                MP.release();
                musicThread.interrupt();
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.sound_off));
            }
            else {
                sound = true;
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.sound));
                musicThread.run();
            }
        }

        if (game_start){

            if (status[2] == 1) {

                score += add_score * extra_rate[difficulty];
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.mole_hit));
                status[2] = 0;
                SEtype = 0;
                SEThread.run();

            } else if (status[2] == 2) {

                // Reduce visibility of marker, as penalty for touching mine
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.mine_blown));
                current_life--;
                score -= add_score * extra_rate[difficulty];
                float alp = marker.getAlpha();

                if (alp > 0) {
                    marker.setAlpha((float) (alp - 0.5));
                }
                SEtype = 1;
                SEThread.run();
            }

            marker.setTag(status);
        }

        else if (status[2] == 21){
            marker.showInfoWindow();
            int[][] figure = new int[][]{{R.drawable.mole, R.drawable.mine}, {R.drawable.mole_hit, R.drawable.mine_blown}};
            marker.setIcon(BitmapDescriptorFactory.fromResource(figure[status[0]][status[1]]));
            SEtype = status[1];
            SEThread.run();
            status[0] = 1 - status[0];
        }

        return false;

    }

// Inspired by https://www.cnblogs.com/gzdaijie/p/5222191.html

    public void showHighScoreDialog() {

        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(MapsActivity.this);
        normalDialog.setIcon(R.drawable.red_star);
        normalDialog.setTitle("Congratulations! You have broken the record.");
        normalDialog.setMessage("Do you wish to update the high score?");
        normalDialog.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        highscoreThread.run();

                        score_board = (TextView) findViewById(R.id.score);
                        score_board.setText("High Score: " + hi_perm + "; your score:" + score);

                        //SharedPreferences.Editor ed = highScore.edit();
                        //ed.putInt("Highscore", hi_temp);
                    }
                });
        normalDialog.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        score = 0;

                    }
                });

        normalDialog.show();
    }

    public void showScoreDialog() {

        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(MapsActivity.this);
        normalDialog.setIcon(R.drawable.red_star);
        normalDialog.setTitle("Game Over");
        normalDialog.setMessage("Your score: " + score + "\nHigh Score: " + hi_perm);
        normalDialog.setPositiveButton("Received",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //SharedPreferences.Editor ed = highScore.edit();
                        //ed.putInt("Highscore", hi_temp);
                    }
                });

        normalDialog.show();
    }

    public void showDifficultyDialog() {
        difficulty = 0;
        AlertDialog.Builder singleChoiceDialog =
                new AlertDialog.Builder(MapsActivity.this);
        singleChoiceDialog.setTitle("Choose difficulty");
        singleChoiceDialog.setIcon(R.drawable.q);

        singleChoiceDialog.setSingleChoiceItems(items, 0,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        difficulty = which;
                    }
                });

        singleChoiceDialog.setPositiveButton("Confirm",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                            Toast.makeText(MapsActivity.this,
                                    "Current Difficulty: " + items[difficulty],

                                    Toast.LENGTH_SHORT).show();

                    }
                });

        singleChoiceDialog.show();

    }

    public void assign() {

        for (int i = 0; i < 16; i ++) {

            try{
                Marker m_temp = marker_list.get(i);
                int[] tag = (int[]) m_temp.getTag();

                double r_1 = Math.random();
                if (r_1 < mole_rate[difficulty]) {
                    double r_2 = Math.random();
                    if (r_2 < mine_rate[difficulty]) {
                        tag[2] = 2;

                        m_temp.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.mine));

                    } else {
                        tag[2] = 1;
                        m_temp.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.mole));
                    }
                }
                else {
                    tag[2] = 0;
                    m_temp.setTag(tag);
                    m_temp.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.q));
                }
            } catch (Exception e) {
                Toast.makeText(MapsActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }

        }

    }

    public void de_assign(boolean game_start) {

        for (int i = 0; i < 16; i ++) {

            try{
                Marker m_temp = marker_list.get(i);
                int[] tag = (int[]) m_temp.getTag();


                if (tag[2] == 2) {
                    score += 1;
                }
                else if (tag[2] == 1) {
                    score += sub_score * reduce_rate[difficulty];
                }

                tag[2] = 0;
                m_temp.setTag(tag);
                m_temp.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.q));

                if (!game_start) {
                    m_temp.setAlpha(1);
                }

            } catch (Exception e) {
                Toast.makeText(MapsActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }

        }

    }

    public void create_marker() {

        for (int i = 0; i < 4; i ++) {
            for (int j = 0; j < 4; j ++) {

                int [] tag = {i, j, 0};
                int x = i * 24 - 36;
                int y = j * 24 - 36;
                LatLng c = new LatLng(x, y);
                Marker m = gMap.addMarker(new MarkerOptions()
                        .position(c)
                        //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.q)));
                m.setTag(tag);
                marker_list.add(m);

            }
        }
    }
}
