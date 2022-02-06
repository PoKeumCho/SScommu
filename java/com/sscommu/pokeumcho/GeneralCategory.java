package com.sscommu.pokeumcho;

import org.json.JSONException;
import org.json.JSONObject;

public class GeneralCategory {

    private static final String JSON_ID = "id";
    private static final String JSON_USERID = "userid";
    private static final String JSON_NAME = "name";
    private static final String JSON_INFO = "info";
    private static final String JSON_EXPEL = "expel";
    private static final String JSON_USERS = "users";

    private int mId;
    private String mUserId;
    private String mName;
    private String mInfo;
    private boolean mExpel;
    private int mUsers;


    /** Constructor */
    // Empty default Constructor
    public GeneralCategory() { }

    public GeneralCategory(JSONObject jo) throws JSONException {

        mId = jo.getInt(JSON_ID);
        mUserId = jo.getString(JSON_USERID);
        mName = jo.getString(JSON_NAME);
        mInfo = jo.getString(JSON_INFO);
        mExpel = jo.getBoolean(JSON_EXPEL);
        mUsers = jo.getInt(JSON_USERS);
    }

    /** Getter & Setter */
    public int getId() { return mId; }
    public void setId(int id) { mId = id; }

    public String getUserId() { return mUserId; }
    public void setUserId(String userId) { mUserId = userId; }

    public String getName() { return mName; }
    public void setName(String name) { mName = name; }

    public String getInfo() { return mInfo; }
    public void setInfo(String info) { mInfo = info; }

    public boolean isExpel() { return mExpel; }
    public void setExpel(boolean expel) { mExpel = expel; }

    public int getUsers() { return mUsers; }
    public void setUsers(int users) { mUsers = users; }

}
