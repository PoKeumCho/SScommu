package com.sscommu.pokeumcho;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DeleteScheduleTask
        extends AsyncTask<Void, Void, String> {

    private ScheduleMainFragment mFragment;
    private ScheduleActivity mActivity;
    private ProgressDialog mProgressDialog;

    private String mUserId;

    private ArrayList<MySchedule> mUserSchedules;
    private ArrayList<CollegeSchedule> mCollegeSchedules;

    private TimetableManager mTimetableManager;

    private boolean mIsValid;
    private String mOption;
    private String mKey;
    private int mIdx;

    private MySchedule mScheduleToDelete;

    public DeleteScheduleTask(ScheduleMainFragment fragment,
                              String userId,
                              ArrayList<MySchedule> userSchedules,
                              ArrayList<CollegeSchedule> collegeSchedules,
                              TimetableManager timetableManager,
                              String idString,
                              int idx) {

        mFragment = fragment;
        mActivity = null;

        mUserId = userId;
        mUserSchedules = userSchedules;
        mCollegeSchedules = collegeSchedules;
        mTimetableManager = timetableManager;

        setVariables(idString);

        mIdx = idx;
    }

    public DeleteScheduleTask(ScheduleActivity activity,
                              String userId,
                              ArrayList<MySchedule> userSchedules,
                              ArrayList<CollegeSchedule> collegeSchedules,
                              TimetableManager timetableManager,
                              String idString,
                              int idx) {

        mFragment = null;
        mActivity = activity;

        mUserId = userId;
        mUserSchedules = userSchedules;
        mCollegeSchedules = collegeSchedules;
        mTimetableManager = timetableManager;

        setVariables(idString);

        mIdx = idx;
    }

    public boolean isValid() { return mIsValid; }

    /** mIsValid, mOption, mKey, mScheduleToDelete 값 설정 */
    private void setVariables(String stringId) {

        mIsValid = false;

        try {
            int id = Integer.parseInt(stringId);

            if (id >= 10000) {
                mOption = "user";
                for (int i = 0; i < mUserSchedules.size(); i++) {
                    if (mUserSchedules.get(i).getId() == id) {
                        mScheduleToDelete = mUserSchedules.get(i);
                        mKey = mUserSchedules.get(i).getClassTime();
                        mIsValid = true;
                    }
                }
            } else {
                for (CollegeSchedule schedule: mCollegeSchedules) {
                    if (schedule.getId() == id) {
                        mScheduleToDelete = schedule;
                        break;
                    }
                }
                mOption = "college";
                mKey = stringId;
                mIsValid = true;
            }

        } catch (Exception exc) {
            // Handle exceptions
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        Context context = mActivity;
        if (context == null)
            context = mFragment.getContext();

        mProgressDialog = ProgressDialog.show(context,
                "시간표 삭제",
                "잠시만 기다려 주세요.",
                false, false);
    }

    @Override
    protected String doInBackground(Void... voids) {

        String result;

        Map<String, String> queryMap = new HashMap();
        queryMap.put("id", mUserId);
        queryMap.put("option", mOption);
        queryMap.put("key", mKey);

        SimpleHttpJSON simpleHttpJSON
                = new SimpleHttpJSON("deleteSchedule", queryMap);
        try {
            result = simpleHttpJSON.sendPost();
        } catch (Exception exc) {
            result = "";
        }
        return result;
    }

    @Override
    protected void onPostExecute(String jsonString) {

        try {
            // Parsing String with JSONTokener to JSONObject
            JSONTokener tokener = new JSONTokener(jsonString);
            JSONObject response = new JSONObject(tokener);

            if (response.getBoolean("result")) {

                mTimetableManager.remove(mIdx);

                if (mOption.equals("user"))
                    mUserSchedules.remove(mScheduleToDelete);
                else if (mOption.equals("college"))
                    mCollegeSchedules.remove(mScheduleToDelete);
            }
        } catch (JSONException exc) {
            // Handle exceptions
        }

        mProgressDialog.dismiss();
    }
}
