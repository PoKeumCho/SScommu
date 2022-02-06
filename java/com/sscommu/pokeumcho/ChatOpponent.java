package com.sscommu.pokeumcho;

import org.json.JSONException;
import org.json.JSONObject;

public class ChatOpponent {

    public static final String JSON_ID = "id";
    public static final String JSON_HAS_NEW_MESSAGE = "hasNewMessage";

    private String mId;
    private boolean mHasNewMessage;

    /** Constructor */

    // Empty default Constructor
    public ChatOpponent() {}

    public ChatOpponent(String id, boolean hasNewMessage) {

        mId = id;
        mHasNewMessage = hasNewMessage;
    }

    public ChatOpponent(JSONObject jo) throws JSONException {

        mId = jo.getString(JSON_ID);
        mHasNewMessage = jo.getBoolean(JSON_HAS_NEW_MESSAGE);
    }

    /** Getter & Setter */
    public String getId() { return mId; }
    public void setId(String id) { mId = id; }

    public boolean hasNewMessage() { return mHasNewMessage; }
    public void hasNewMessage(boolean hasNewMessage) { mHasNewMessage = hasNewMessage; }
}
