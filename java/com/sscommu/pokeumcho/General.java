package com.sscommu.pokeumcho;

import android.content.Intent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class General {

    private static final String JSON_ID = "id";
    private static final String JSON_USER_ID = "userid";
    private static final String JSON_USER_ACCOUNT_IMG = "user_accountimg";
    private static final String JSON_USER_NICKNAME = "user_nickname";
    private static final String JSON_CATEGORY_ID = "categoryid";
    private static final String JSON_CATEGORY_NAME = "category";
    private static final String JSON_TEXT = "text";
    private static final String JSON_IMG = "img";
    private static final String JSON_DATE = "date";
    private static final String JSON_LIKES = "likes";
    private static final String JSON_COMMENTS = "comments";
    private static final String JSON_GROUP_ID = "groupid";

    private int mId;
    private String mUserId;
    private int mUserAccountImg;
    private String mUserNickname;
    private int mCategoryId;
    private String mCategoryName;
    private String mText;
    private int mImg;
    private String mDate;
    private int mLikes;
    private int mComments;
    private int mGroupId;


    /** Constructor */
    // Empty default Constructor
    public General() { }

    public General(JSONObject jo) throws JSONException {

        mId = jo.getInt(JSON_ID);
        mUserId = jo.getString(JSON_USER_ID);
        mUserAccountImg = jo.getInt(JSON_USER_ACCOUNT_IMG);
        mUserNickname = jo.getString(JSON_USER_NICKNAME);
        mCategoryId = jo.getInt(JSON_CATEGORY_ID);
        mCategoryName = jo.getString(JSON_CATEGORY_NAME);
        mText = jo.getString(JSON_TEXT);
        mImg = jo.getInt(JSON_IMG);
        mDate = jo.getString(JSON_DATE);
        mLikes = jo.getInt(JSON_LIKES);
        mComments = jo.getInt(JSON_COMMENTS);
        mGroupId = jo.getInt(JSON_GROUP_ID);
    }

    // 채팅 구현 부분에서 사용
    public boolean isWithdrawalUser() { return (mUserAccountImg == -1); }

    /** Getter & Setter */
    public int getId() { return mId; }
    public void setId(int id) { mId = id; }

    public String getUserId() { return mUserId; }
    public void setUserId(String userId) { mUserId = userId; }

    public int getUserAccountImg() {
        if (mUserAccountImg == -1) {
            return R.drawable.account_img_ghost;
        } else {
            return User.ACCOUNT_IMAGES[(mUserAccountImg - 1)];
        }
    }

    public String getUserNickname() { return mUserNickname; }
    public void setUserNickname(String nickname) { mUserNickname = nickname; }

    public int getCategoryId() { return mCategoryId; }
    public void setCategoryId(int categoryId) { mCategoryId = categoryId; }

    public String getCategoryName() { return mCategoryName; }
    public void setCategoryName(String categoryName) { mCategoryName = categoryName; }

    public String getText() { return mText; }
    public void setText(String text) { mText = text; }

    public int getImgCount() { return mImg; }
    public void setImgCount(int count) { mImg = count; }

    public String getDate() { return mDate; }
    public void setDate(String date) { mDate = date; }

    public int getLikes() { return mLikes; }
    public void setLikes(int likes) { mLikes = likes; }

    public int getComments() { return mComments; }
    public void setComments(int comments) { mComments = comments; }

    public int getGroupId() { return mGroupId; }
    public void setGroupId(int groupId) { mGroupId = groupId; }


    /** Formatted */
    public String getDateLikeEveryTime() {
        MyDate date = new MyDate(mDate);
        return date.getDateLikeEveryTime(new Date());
    }
    public String getFormattedDate() {
        MyDate date = new MyDate(mDate);
        return date.getFormattedDate();
    }

    public String getLikesString() {
        MyNumber number = new MyNumber(mLikes);
        return number.getKM_Advanced();
    }

    public String getCommentsString() {
        MyNumber number = new MyNumber(mComments);
        return number.getKM();
    }


    static public void putIntentData(Intent intent, General general) {

        intent.putExtra("GENERAL_ID", general.mId);
        intent.putExtra("GENERAL_USER_ID", general.mUserId);
        intent.putExtra("GENERAL_USER_ACCOUNT_IMG", general.mUserAccountImg);
        intent.putExtra("GENERAL_USER_NICKNAME", general.mUserNickname);
        intent.putExtra("GENERAL_CATEGORY_ID", general.mCategoryId);
        intent.putExtra("GENERAL_CATEGORY_NAME", general.mCategoryName);
        intent.putExtra("GENERAL_TEXT", general.mText);
        intent.putExtra("GENERAL_IMG", general.mImg);
        intent.putExtra("GENERAL_DATE", general.mDate);
        intent.putExtra("GENERAL_LIKES", general.mLikes);
        intent.putExtra("GENERAL_COMMENTS", general.mComments);
        intent.putExtra("GENERAL_GROUP_ID ", general.mGroupId);
    }

    static public General createGeneral(Intent intent) {

        General general = new General();

        general.mId = intent.getIntExtra("GENERAL_ID", 0);
        general.mUserId = intent.getStringExtra("GENERAL_USER_ID");
        general.mUserAccountImg = intent.getIntExtra("GENERAL_USER_ACCOUNT_IMG", 1);
        general.mUserNickname = intent.getStringExtra("GENERAL_USER_NICKNAME");
        general.mCategoryId = intent.getIntExtra("GENERAL_CATEGORY_ID", 0);
        general.mCategoryName = intent.getStringExtra("GENERAL_CATEGORY_NAME");
        general.mText = intent.getStringExtra("GENERAL_TEXT");
        general.mImg = intent.getIntExtra("GENERAL_IMG", 0);
        general.mDate = intent.getStringExtra("GENERAL_DATE");
        general.mLikes = intent.getIntExtra("GENERAL_LIKES", 0);
        general.mComments = intent.getIntExtra("GENERAL_COMMENTS", 0);
        general.mGroupId = intent.getIntExtra("GENERAL_GROUP_ID", 0);

        if (general.mId == 0 || general.mCategoryId == 0) {
            return null;
        } else {
            return general;
        }
    }
}
