package com.app.assignment5ms;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

class ActivitiesAdapter extends ArrayAdapter<DetectedActivity> {

    HashMap<Integer, Integer> detectedActivitiesMap;


    ArrayList<ArrayList<Integer>> completeActivitesMap;

    ActivitiesAdapter(Context context,
                      ArrayList<DetectedActivity> detectedActivities) {
        super(context, 0, detectedActivities);
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {

        //Retrieve the data item//
        DetectedActivity detectedActivity = getItem(position);
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(
                    R.layout.detected_activity, parent, false);
        }
        //Retrieve the TextViews where we’ll display the activity type, and percentage//

        TextView activityName = (TextView) view.findViewById(R.id.activity_type);
        TextView activityConfidenceLevel = (TextView) view.findViewById(
                R.id.confidence_percentage);
        //If an activity is detected...//
        if (detectedActivity != null) {
            activityName.setText(ActivityIntentService.getActivityString(getContext(),
                //...get the activity type...//
                    detectedActivity.getType()));
            //..and the confidence percentage//
            activityConfidenceLevel.setText(getContext().getString(R.string.percentage,
                    detectedActivity.getConfidence()));

        }
        return view;
    }
    //Process the list of detected activities//
    void updateActivities(ArrayList<DetectedActivity> detectedActivities) {
        detectedActivitiesMap = new HashMap<>();
        for (DetectedActivity activity : detectedActivities) {
            detectedActivitiesMap.put(activity.getType(), activity.getConfidence());
        }

        ArrayList<DetectedActivity> temporaryList = new ArrayList<>();
        for (int i = 0; i < ActivityIntentService.POSSIBLE_ACTIVITIES.length; i++) {
            int confidence = detectedActivitiesMap.containsKey(ActivityIntentService.POSSIBLE_ACTIVITIES[i]) ?
                    detectedActivitiesMap.get(ActivityIntentService.POSSIBLE_ACTIVITIES[i]) : 0;

            //Add the object to a temporaryList//
            temporaryList.add(new
                    DetectedActivity(ActivityIntentService.POSSIBLE_ACTIVITIES[i],
                    confidence));
        }
        //Remove all elements from the temporaryList//
        this.clear();
        //Refresh the View//

        for (DetectedActivity detectedActivity: temporaryList) {
            this.add(detectedActivity);
        }

        if (completeActivitesMap == null) {
            completeActivitesMap = new ArrayList<>();
        }

        ArrayList tempActivity = new ArrayList<Integer>();
        int tempConfidence = 0;
        int tempActivityType = 0;
        for (int i = 0; i < detectedActivities.size(); i++) {
            if (detectedActivities.get(i).getConfidence() > tempConfidence) {
                tempConfidence = detectedActivities.get(i).getConfidence();
                tempActivityType =  detectedActivities.get(i).getType();
            }
        }
        Long tsLong = System.currentTimeMillis()/1000;

        tempActivity.add(0, tempActivityType);
        tempActivity.add(1, tempConfidence);
        tempActivity.add(2, tsLong.toString());


        completeActivitesMap.add(tempActivity);

    }

    public ArrayList<ArrayList<Integer>> getCompleteActivitesMap() {
        return completeActivitesMap;
    }
}