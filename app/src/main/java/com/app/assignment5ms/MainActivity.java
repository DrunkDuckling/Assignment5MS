package com.app.assignment5ms;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private Context mContext;
    public static final String DETECTED_ACTIVITY = ".DETECTED_ACTIVITY";
    //Define an ActivityRecognitionClient//

    private ActivityRecognitionClient mActivityRecognitionClient;
    private ActivitiesAdapter mAdapter;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        //Retrieve the ListView where we’ll display our activity data//
        ListView detectedActivitiesListView = (ListView) findViewById(R.id.activities_listview);

        ArrayList<DetectedActivity> detectedActivities = ActivityIntentService.detectedActivitiesFromJson(
                PreferenceManager.getDefaultSharedPreferences(this).getString(
                        DETECTED_ACTIVITY, ""));

        //Bind the adapter to the ListView//
        mAdapter = new ActivitiesAdapter(this, detectedActivities);
        detectedActivitiesListView.setAdapter(mAdapter);
        mActivityRecognitionClient = new ActivityRecognitionClient(this);

        Task<Void> task = mActivityRecognitionClient.requestActivityUpdates(
                1000,
                getActivityDetectionPendingIntent());

        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                updateDetectedActivitiesList();
                Log.d("debugging", "Updated Activity Recognition Guesses");
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                final String started = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
                final long startedTime = System.currentTimeMillis();
                while (true){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String elapsed = Long.toString((System.currentTimeMillis() - startedTime)/1000);
                            TextView elapsedView = (TextView) findViewById(R.id.time_tv);
                            elapsedView.setText("Started at: " + started + "\nBeen Running for: " + elapsed + " seconds.");
                        }
                    });

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();


    }
    @Override
    protected void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
        updateDetectedActivitiesList();
    }
    @Override
    protected void onPause() {
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }
    public void requestUpdatesHandler(View view) {
        //Set the activity detection interval. I’m using 1 seconds//
        Task<Void> task = mActivityRecognitionClient.requestActivityUpdates(
                1000,
                getActivityDetectionPendingIntent());
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                updateDetectedActivitiesList();
                System.out.println("UPDATED");
            }
        });
    }
    //Get a PendingIntent//
    private PendingIntent getActivityDetectionPendingIntent() {
        //Send the activity data to our DetectedActivitiesIntentService class//
        Intent intent = new Intent(this, ActivityIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

    }
    //Process the list of activities//
    protected void updateDetectedActivitiesList() {
        ArrayList<DetectedActivity> detectedActivities = ActivityIntentService.detectedActivitiesFromJson(
                PreferenceManager.getDefaultSharedPreferences(mContext)
                        .getString(DETECTED_ACTIVITY, ""));

        mAdapter.updateActivities(detectedActivities);
    }
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(DETECTED_ACTIVITY)) {
            updateDetectedActivitiesList();
        }

    }

    public void exportCsv(View view) {
        String filename = "data.csv";
        FileOutputStream outputStream;

        try{
            //Creating file
            /*FileOutputStream out = openFileOutput("data.csv", Context.MODE_PRIVATE);
            out.write((data.toString()).getBytes());
            out.close();*/

            MemoryList ram = ActivityIntentService.activitiesInMemory;
            String content = "ACTIVITY,PERCENTAGE,TIMESTAMP\n";
            for(String[] act : ram.getAll()){
                content += String.format("%s,%s,%s\n", act[0], act[1], act[2]);
            }
            Log.d("debugging", content);

            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);

            outputStream.write((content).getBytes());

/*            outputStream.write(("TYPE" + "," + "CONFIDENCE" +  "," + "TIMESTAMP" + "\n").getBytes());*/

            /*for(int i = 0; i < mAdapter.getCompleteActivitesMap().size(); i++) {
                outputStream.write((mAdapter.getCompleteActivitesMap().get(i).get(0) + ",").getBytes());
                outputStream.write((mAdapter.getCompleteActivitesMap().get(i).get(1) + ",").getBytes());
                outputStream.write((mAdapter.getCompleteActivitesMap().get(i).get(2) + "\n").getBytes());
*//*
                outputStream.write((getValues.get(i)+",").getBytes());
                outputStream.write((getValues.get(i+1)+",").getBytes());
                outputStream.write((getValues.get(i+2)+"\n").getBytes());
*//*
            }*/
            outputStream.close();

            //exporting
            Context context = getApplicationContext();
            File fileLocation = new File(getFilesDir(), "data.csv");
            Uri path = FileProvider.getUriForFile(context, "com.app.assignment5ms.fileprovider", fileLocation);
            Intent fileIntent = new Intent(Intent.ACTION_SEND);
            fileIntent.setType("text/csv");
            fileIntent.putExtra(Intent.EXTRA_SUBJECT, "Data");
            fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            fileIntent.putExtra(Intent.EXTRA_STREAM, path);
            startActivity(Intent.createChooser(fileIntent, "Send mail"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}