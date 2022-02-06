package com.sscommu.pokeumcho;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/** Get And Display Schedules */
public class GetScheduleTask
        extends AsyncTask<Void, Void, String> {

    private String mUserId;

    private ArrayList<MySchedule> mUserSchedules;
    private ArrayList<CollegeSchedule> mCollegeSchedules;

    private TimetableManager mTimetableManager;

    public GetScheduleTask(String userId,
                           ArrayList<MySchedule> userSchedules,
                           ArrayList<CollegeSchedule> collegeSchedules,
                           TimetableManager timetableManager) {

        mUserId = userId;
        mUserSchedules = userSchedules;
        mCollegeSchedules = collegeSchedules;
        mTimetableManager = timetableManager;
    }

    @Override
    protected String doInBackground(Void... voids) {

        String result;

        Map<String, String> queryMap = new HashMap();
        queryMap.put("id", mUserId);

        SimpleHttpJSON simpleHttpJSON
                = new SimpleHttpJSON("mySchedule", queryMap);
        try {
            result = simpleHttpJSON.sendPost();
        } catch (Exception exc) {
            result = "";
        }
        return result;
    }

    @Override
    protected void onPostExecute(String jsonString) {

        // 기존 값 초기화
        mTimetableManager.clear();
        mUserSchedules.clear();
        mCollegeSchedules.clear();

        try {
            // Parsing String with JSONTokener to JSONObject
            JSONTokener tokener = new JSONTokener(jsonString);
            JSONObject response = new JSONObject(tokener);

            if (response.getBoolean("result")) {

                JSONArray jArray;

                jArray = response.getJSONArray("user_schedule");
                for (int i = 0; i < jArray.length(); i++) {
                    MySchedule schedule = new MySchedule(jArray.getJSONObject(i));
                    mUserSchedules.add(schedule);
                    mTimetableManager.addSchedule(schedule);
                }

                jArray = response.getJSONArray("schedule");
                for (int i = 0; i < jArray.length(); i++) {
                    CollegeSchedule schedule = new CollegeSchedule(jArray.getJSONObject(i));
                    mCollegeSchedules.add(schedule);
                    mTimetableManager.addSchedule(schedule);
                }

            } else {
                // Handle false result
            }
        } catch (JSONException exc) {
            // Handle exceptions
        }
    }
}
