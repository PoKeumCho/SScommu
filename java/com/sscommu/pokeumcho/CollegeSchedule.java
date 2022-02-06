package com.sscommu.pokeumcho;

import org.json.JSONException;
import org.json.JSONObject;

public class CollegeSchedule extends MySchedule {

    private static final String JSON_NO = "no";
    private static final String JSON_SUBJECTS = "subjects";
    private static final String JSON_DEPARTMENT = "department";
    private static final String JSON_CLASS_NUMBER = "classNumber";
    private static final String JSON_BUNBAN = "bunban";
    private static final String JSON_ISUGUBUN = "isugubun";
    private static final String JSON_CAMPUS = "campus";
    private static final String JSON_ROOM_AND_PROF = "roomAndProf";

    private int mNo;
    private String mSubjects;
    private String mDepartment;
    private String mClassNumber;
    private int mBunban;
    private String mIsugubun;
    private String mCampus;
    private String mRoomAndProf;

    /** Constructor */
    // Empty default Constructor
    public CollegeSchedule() { }

    public CollegeSchedule(JSONObject jo) throws JSONException {

        mNo = jo.getInt(JSON_NO);
        mSubjects = jo.getString(JSON_SUBJECTS);
        mDepartment = jo.getString(JSON_DEPARTMENT);
        mClassNumber = jo.getString(JSON_CLASS_NUMBER);
        setClassName(jo.getString(JSON_CLASS_NAME));
        mBunban = jo.getInt(JSON_BUNBAN);
        mIsugubun = jo.getString(JSON_ISUGUBUN);
        setClassTime(jo.getString(JSON_CLASS_TIME));
        mCampus = jo.getString(JSON_CAMPUS);
        mRoomAndProf = jo.getString(JSON_ROOM_AND_PROF);

        setId(mNo);
        setClassInfo("\n" + mCampus + "\n" + mRoomAndProf);
    }

    public boolean isClassNumberMatch(String search) {

        if (mClassNumber.indexOf(search) < 0)
            return false;
        else
            return true;
    }

    /** ScheduleAdapter */

    public String getTextLine1() {   // className
        return getClassName();
    }

    public String getTextLine2() {  // 분반 • classNumber
        if (!mClassNumber.equals(""))
            return "분반 " + mBunban + " • " + mClassNumber;
        else
            return "분반 " + mBunban;
    }

    public String getTextLine3() {  // subjects • department • 이수구분
        StringBuilder sb = new StringBuilder();
        if (!mSubjects.equals(""))
            sb.append(mSubjects + " • ");
        if (!mDepartment.equals(""))
            sb.append(mDepartment + " • ");
        if (!mIsugubun.equals(""))
            sb.append(mIsugubun);
        return sb.toString().replaceFirst("\\s•\\s$", "");
    }

    public String getTextLine4() {  // campus • classTime
        return mCampus + " • " + getClassTime();
    }

    public String getTextLine5() {  // roomAndProf
        return mRoomAndProf;
    }
}
