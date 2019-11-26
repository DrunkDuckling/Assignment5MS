package com.app.assignment5ms;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import com.app.assignment5ms.service.ActivityIntentService;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ListView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private Context mContext;
    public static final String DETECTED_ACTIVITY = ".DETECTED_ACTIVITY";
    //Define an ActivityRecognitionClient//

    private ActivityRecognitionClient mActivityRecognitionClient;
    private ActivitiesAdapter mAdapter;

    //ArrayList for storing activity data
    private ArrayList<DetectedActivity> mActivityData;
    HashMap<Integer, Integer> activitiesMap = new HashMap<>();

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

        // Testing
        Task<Void> task = mActivityRecognitionClient.requestActivityUpdates(
                3000,
                getActivityDetectionPendingIntent());
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                updateDetectedActivitiesList();
            }
        });
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

    // TODO Deactivated for now
    public void requestUpdatesHandler(View view) {
        //Set the activity detection interval. I’m using 3 seconds//
        Task<Void> task = mActivityRecognitionClient.requestActivityUpdates(
                3000,
                getActivityDetectionPendingIntent());
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                updateDetectedActivitiesList();
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

        // place data into new array list
        for (DetectedActivity activity : detectedActivities) {
            activitiesMap.put(activity.getType(), activity.getConfidence());
        }

        mAdapter.updateActivities(detectedActivities);


    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(DETECTED_ACTIVITY)) {
            updateDetectedActivitiesList();
        }
    }

    public void exportCsv(View view){
        //generate data
        StringBuilder data = new StringBuilder();
        data.append("Time,Distance");
        for(int i = 0; i<5; i++){
            data.append("\n"+ i +","+ i*i);
        }

        String filename = "csvFile.csv";
        FileOutputStream outputStream;

        ArrayList<DetectedActivity> temporaryList = new ArrayList<>();

        for (int i = 0; i < ActivityIntentService.POSSIBLE_ACTIVITIES.length; i++) {
            int confidence = activitiesMap.containsKey(ActivityIntentService.POSSIBLE_ACTIVITIES[i]) ?
                    activitiesMap.get(ActivityIntentService.POSSIBLE_ACTIVITIES[i]) : 0;

            //Add the object to a temporaryList//
            temporaryList.add(new
                    DetectedActivity(ActivityIntentService.POSSIBLE_ACTIVITIES[i],
                    confidence));
            System.out.println("TEST!: " + ActivityIntentService.POSSIBLE_ACTIVITIES[i] + confidence);
        }

        System.out.println("TESTING: " + temporaryList);

        try{
            //Creating file
            FileOutputStream out = openFileOutput("data.csv", Context.MODE_PRIVATE);
            out.write((data.toString()).getBytes());
            out.close();

            /*outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            for(int i = 0; i < uAverage.size(); i+=3) {
                outputStream.write((getValues.get(i)+",").getBytes());
                outputStream.write((getValues.get(i+1)+",").getBytes());
                outputStream.write((getValues.get(i+2)+"\n").getBytes());
            }
            outputStream.close();*/

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
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}
