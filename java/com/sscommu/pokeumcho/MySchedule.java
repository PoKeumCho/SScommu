package com.sscommu.pokeumcho;

import org.json.JSONException;
import org.json.JSONObject;

public class MySchedule {

    private static int ID_GENERATOR = 10000;

    public static final String JSON_CLASS_NAME = "className";
    public static final String JSON_CLASS_TIME = "classTime";
    private static final String JSON_CLASS_INFO = "classInfo";

    private int mId;                // Identification number for deletion
    private String mClassName;
    private String mClassTime;
    private String mClassInfo;

    /** Constructor */
    // Empty default Constructor
    public MySchedule() { }

    public MySchedule(String classTime) { mClassTime = classTime; }

    public MySchedule(
            String className, String classTime, String classInfo) {

        mId = ID_GENERATOR++;
        mClassName = className;
        mClassTime = classTime;
        mClassInfo = classInfo;
    }

    public MySchedule(JSONObject jo) throws JSONException {

        mId = ID_GENERATOR++;
        mClassName = jo.getString(JSON_CLASS_NAME);
        mClassTime = jo.getString(JSON_CLASS_TIME);
        mClassInfo = "\n" + jo.getString(JSON_CLASS_INFO);
    }

    /** Getter & Setter */
    public void setId(int id) { mId = id; }
    public int getId() { return mId; }

    public void setClassName(String className) { mClassName = className; }
    public String getClassName() { return mClassName; }

    public void setClassTime(String classTime) { mClassTime = classTime; }
    public String getClassTime() { return mClassTime; }

    public void setClassInfo(String classInfo) { mClassInfo = classInfo; }
    public String getClassInfo() { return mClassInfo; }


    public boolean isClassNameMatch(String search) {

        if (mClassName.indexOf(search) < 0)
            return false;
        else
            return true;
    }
}
