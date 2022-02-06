package com.sscommu.pokeumcho;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddScheduleTask
        extends AsyncTask<Void, Void, String> {

    private ScheduleActivity mActivity;
    private ProgressDialog mProgressDialog;

    private String mUserId;

    private ArrayList<MySchedule> mUserSchedules;
    private ArrayList<CollegeSchedule> mCollegeSchedules;

    private TimetableManager mTimetableManager;

    private MySchedule mScheduleToAdd;

    public AddScheduleTask(ScheduleActivity activity,
                           String userId,
                           ArrayList<MySchedule> userSchedules,
                           ArrayList<CollegeSchedule> collegeSchedules,
                           TimetableManager timetableManager,
                           MySchedule scheduleToAdd) {

        mActivity = activity;
        mUserId = userId;
        mUserSchedules = userSchedules;
        mCollegeSchedules = collegeSchedules;
        mTimetableManager = timetableManager;
        mScheduleToAdd = scheduleToAdd;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        mProgressDialog = ProgressDialog.show(mActivity,
                "시간표 추가",
                "잠시만 기다려 주세요.",
                false, false);
    }

    @Override
    protected String doInBackground(Void... voids) {

        String result;

        Map<String, String> queryMap = new HashMap();
        queryMap.put("id", mUserId);
        queryMap.put("class_id", String.valueOf(mScheduleToAdd.getId()));
        queryMap.put("class_time", mScheduleToAdd.getClassTime());
        queryMap.put("class_name", mScheduleToAdd.getClassName());
        queryMap.put("class_info", mScheduleToAdd.getClassInfo());

        SimpleHttpJSON simpleHttpJSON
                = new SimpleHttpJSON("addSchedule", queryMap);
        try {
            result = simpleHttpJSON.sendPost();
        } catch (Exception exc) {
            result = "";
        }
        return result;
    }

    @Override
    protected void onPostExecute(String jsonString) {
        super.onPostExecute(jsonString);

        try {
            // Parsing String with JSONTokener to JSONObject
            JSONTokener tokener = new JSONTokener(jsonString);
            JSONObject response = new JSONObject(tokener);

            if (response.getBoolean("result")) {

                // 기존 값 초기화
                mTimetableManager.clear();
                mUserSchedules.clear();
                mCollegeSchedules.clear();

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
            // Handle json exceptions
        }

        mProgressDialog.dismiss();
    }
}
