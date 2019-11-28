package com.app.assignment5ms;

import java.util.ArrayList;
import java.lang.reflect.Type;
import android.content.Context;

import com.app.assignment5ms.MainActivity;
import com.app.assignment5ms.R;
import com.google.gson.Gson;
import android.content.Intent;
import android.app.IntentService;
import android.preference.PreferenceManager;
import android.content.res.Resources;
import android.util.Log;

import com.google.gson.reflect.TypeToken;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

//Extend IntentService//
public class ActivityIntentService extends IntentService {

    protected static final String TAG = "IntentService";
    public static MemoryList activitiesInMemory = new MemoryList() ;

    //Call the super IntentService constructor with the name for the worker thread//
    public ActivityIntentService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    //Define an onHandleIntent() method, which will be called whenever an activity detection update is available//
    @Override
    protected void onHandleIntent(Intent intent) {
        //Check whether the Intent contains activity recognition data//
        if (ActivityRecognitionResult.hasResult(intent)) {

            //If data is available, then extract the ActivityRecognitionResult from the Intent//
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

            //Get an array of DetectedActivity objects//
            ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();
            PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putString(MainActivity.DETECTED_ACTIVITY,
                            detectedActivitiesToJson(detectedActivities)).apply();

            // Gets the Int's corresponding with the activity
            DetectedActivity mostProbableActivity = result.getMostProbableActivity();
            String confidence = Integer.toString(mostProbableActivity.getConfidence());
            int activityType = mostProbableActivity.getType();
            determineActivityAndAddToLog(confidence, activityType);

            /*// Log each activity.
            Log.i(TAG, "activities detected");
            for (DetectedActivity da : detectedActivities) {
                Log.i(TAG, getActivityString(
                        getApplicationContext(),
                        da.getType()) + " " + da.getConfidence() + "%"
                );
            }*/
        }


    }

    // get type and confidence and save it.
    private void determineActivityAndAddToLog(String confidence, int type){

        String ActivityType = "";

        if (type == DetectedActivity.ON_BICYCLE)
            ActivityType = "ON_BICYCLE";
        else if(type == DetectedActivity.ON_FOOT)
            ActivityType = "STANDING";
        else if(type == DetectedActivity.RUNNING)
            ActivityType = "RUNNING";
        else if(type == DetectedActivity.STILL)
            ActivityType = "STILL";
        else if(type == DetectedActivity.TILTING)
            ActivityType = "TILTING";
        else if(type == DetectedActivity.WALKING)
            ActivityType = "WALKING";
        else if(type == DetectedActivity.IN_VEHICLE)
            ActivityType = "IN_VEHICLE";
        else
            ActivityType = "UNKNOWN";

        activitiesInMemory.add(ActivityType, confidence);
    }

//Convert the code for the detected activity type, into the corresponding string//
    public static String getActivityString(Context context, int detectedActivityType) {
        Resources resources = context.getResources();
        switch(detectedActivityType) {
            case DetectedActivity.ON_BICYCLE:
                return resources.getString(R.string.bicycle);
            case DetectedActivity.ON_FOOT:
                return resources.getString(R.string.foot);
            case DetectedActivity.RUNNING:
                return resources.getString(R.string.running);
            case DetectedActivity.STILL:
                return resources.getString(R.string.still);
            case DetectedActivity.TILTING:
                return resources.getString(R.string.tilting);
            case DetectedActivity.WALKING:
                return resources.getString(R.string.walking);
            case DetectedActivity.IN_VEHICLE:
                return resources.getString(R.string.vehicle);
            default:
                return resources.getString(R.string.unknown_activity);
        }
    }
    public static final int[] POSSIBLE_ACTIVITIES = {

            /*
            IN_VEHICLE  = 0;
            ON_BICYCLE  = 1;
            ON_FOOT     = 2;
            STILL       = 3;
            UNKNOWN     = 4;
            TILTING     = 5;
            WALKING     = 7;
            RUNNING     = 8;
            */

            DetectedActivity.STILL,
            DetectedActivity.ON_FOOT,
            DetectedActivity.WALKING,
            DetectedActivity.RUNNING,
            DetectedActivity.IN_VEHICLE,
            DetectedActivity.ON_BICYCLE,
            DetectedActivity.TILTING,
            DetectedActivity.UNKNOWN
    };
    public static String detectedActivitiesToJson(ArrayList<DetectedActivity> detectedActivitiesList) {
        Type type = new TypeToken<ArrayList<DetectedActivity>>() {}.getType();
        return new Gson().toJson(detectedActivitiesList, type);
    }
    public static ArrayList<DetectedActivity> detectedActivitiesFromJson(String jsonArray) {
        Type listType = new TypeToken<ArrayList<DetectedActivity>>(){}.getType();
        ArrayList<DetectedActivity> detectedActivities = new Gson().fromJson(jsonArray, listType);
        if (detectedActivities == null) {
            detectedActivities = new ArrayList<>();
        }
        return detectedActivities;
    }
}