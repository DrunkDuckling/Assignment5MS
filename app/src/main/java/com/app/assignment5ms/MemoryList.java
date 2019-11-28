package com.app.assignment5ms;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MemoryList  {

    private ArrayList<String[]> activities;

    public MemoryList(){
        activities = new ArrayList<>();
    }

    public void add(String confidence, String ActivityType){
        activities.add(new String[]{ActivityType, confidence, new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date())});
    }

    public ArrayList<String[]> getAll(){
        return this.activities;
    }

    @Override
    public String toString(){

        String info = "";
        for (String[] activity : activities){
            info += "{";
            for(String currentActivity : activity){
                if (!currentActivity.contains(" ")){
                    info += currentActivity + ", ";
                } else {
                    info += currentActivity;
                }
            }
            info += "}, ";
        }
        return "MemoryList(" + info + ")";
    }
}
