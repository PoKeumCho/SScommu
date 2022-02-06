package com.sscommu.pokeumcho;

import android.graphics.Bitmap;

import org.json.JSONException;
import org.json.JSONObject;

public class ChatData {

    public static final String JSON_ID = "no";
    public static final String JSON_SENDER_ID = "senderid";
    public static final String JSON_DATE = "datetime";
    public static final String JSON_CONTENT_TYPE = "contenttype";
    public static final String JSON_READ_STATUS = "readstatus";
    public static final String JSON_CONTENT = "content";

    public enum ContentType { TEXT, FILE };

    public class ViewType {

        public static final int ME_TEXT = 0;
        public static final int ME_FILE = 1;
        public static final int OTHER_TEXT = 2;
        public static final int OTHER_FILE = 3;
        public static final int DATE = 4;
    }

    private int mId;
    private String mDate;
    private ContentType mContentType;
    private boolean mReadStatus;
    private String mContent;

    private int mViewType;

    private Bitmap mBitmap;     // FILE

    /** Constructor */

    // Empty default Constructor
    public ChatData() {}

    public ChatData(String date, boolean readStatus) {
        mDate = date;
        mReadStatus = readStatus;
        mViewType = ViewType.DATE;
    }

    public ChatData(JSONObject jo, String userId) throws JSONException {

        String senderId = jo.getString(JSON_SENDER_ID);

        mId = jo.getInt(JSON_ID);
        mDate = jo.getString(JSON_DATE);
        mContentType = jo.getString(JSON_CONTENT_TYPE).equals("T") ?
                ContentType.TEXT : ContentType.FILE;
        mReadStatus = jo.getBoolean(JSON_READ_STATUS);
        mContent = jo.getString(JSON_CONTENT);

        if (senderId.equals(userId)) {
            if (mContentType == ContentType.TEXT)
                mViewType = ViewType.ME_TEXT;   // 사용자가 보낸 텍스트
            else
                mViewType = ViewType.ME_FILE;   // 사용자가 보낸 이미지
        } else {
            if (mContentType == ContentType.TEXT)
                mViewType = ViewType.OTHER_TEXT;   // 상대방이 보낸 텍스트
            else
                mViewType = ViewType.OTHER_FILE;   // 상대방이 보낸 이미지
        }
    }

    /** Getter & Setter */
    public int getId() { return mId; }
    public void setId(int id) { mId = id; }

    public String getDate() { return mDate; }
    public void setDate(String date) { mDate = date; }

    public ContentType getContentType() { return mContentType; }
    public void setContentType(ContentType contentType) { mContentType = contentType; }

    public boolean getReadStatus() { return mReadStatus; }
    public void setReadStatus(boolean readStatus) { mReadStatus = readStatus; }

    public String getContent() { return mContent; }
    public void setContent(String content) { mContent = content; }

    public int getViewType() { return mViewType; }
    public void setViewType(int viewType) { mViewType = viewType; }

    public Bitmap getBitmap() { return mBitmap; }
    public void setBitmap(Bitmap bitmap) { mBitmap = bitmap; }
}
