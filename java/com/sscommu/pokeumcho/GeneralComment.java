package com.sscommu.pokeumcho;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class GeneralComment {

    private static final String JSON_ID = "id";
    private static final String JSON_USER_ID = "userid";
    private static final String JSON_USER_ACCOUNT_IMG = "user_accountimg";
    private static final String JSON_USER_NICKNAME = "user_nickname";
    private static final String JSON_TEXT = "text";
    private static final String JSON_DATE = "date";
    private static final String JSON_LIKES = "likes";
    private static final String JSON_CLASS = "class";
    private static final String JSON_GROUP = "group";
    private static final String JSON_CATEGORY_ID = "categoryid";
    private static final String JSON_GENERAL_ID = "generalid";
    private static final String JSON_COMMENT = "comment";

    private int mId;
    private String mUserId;
    private int mUserAccountImg;
    private String mUserNickname;
    private String mText;
    private String mDate;
    private int mLikes;
    private int mClass;
    private int mGroup;
    private int mCategoryId;
    private int mGeneralId;

    private ArrayList<GeneralComment> mComments; // 대댓글

    /** Constructor */
    // Empty default Constructor
    public GeneralComment() { }

    public GeneralComment(JSONObject jo) throws JSONException {

        mId = jo.getInt(JSON_ID);
        mUserId = jo.getString(JSON_USER_ID);
        mUserAccountImg = jo.getInt(JSON_USER_ACCOUNT_IMG);
        mUserNickname = jo.getString(JSON_USER_NICKNAME);
        mText = jo.getString(JSON_TEXT);
        mDate = jo.getString(JSON_DATE);
        mLikes = jo.getInt(JSON_LIKES);
        mClass = jo.getInt(JSON_CLASS);
        mGroup = jo.getInt(JSON_GROUP);
        mCategoryId = jo.getInt(JSON_CATEGORY_ID);
        mGeneralId = jo.getInt(JSON_GENERAL_ID);

        try {
            JSONArray jArray = jo.getJSONArray(JSON_COMMENT);
            mComments = new ArrayList<GeneralComment>();
            for (int i = 0; i < jArray.length(); i++) {
                mComments.add(new GeneralComment(jArray.getJSONObject(i)));
            }
        } catch (JSONException exc) {
            mComments = null;
        }
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

    public String getText() { return mText; }
    public void setText(String text) { mText = text; }

    public String getDate() { return mDate; }
    public void setDate(String date) { mDate = date; }

    public int getLikes() { return mLikes; }
    public void setLikes(int likes) { mLikes = likes; }

    public int getCommentClass() { return mClass; }
    public void setCommentClass(int commentClass) { mClass = commentClass; }

    public int getGroup() { return mGroup; }
    public void setGroup(int group) { mGroup = group; }

    public int getCategoryId() { return mCategoryId; }
    public void setCategoryId(int categoryId) { mCategoryId = categoryId; }

    public int getGeneralId() { return mGeneralId; }
    public void setGeneralId(int generalId) { mGeneralId = generalId; }

    public ArrayList<GeneralComment> getComments() { return mComments; }


    /** Formatted */
    public String getFormattedDate() {
        MyDate date = new MyDate(mDate);
        SimpleDateFormat format = new SimpleDateFormat("yy/MM/dd KK:mm");
        return date.getFormattedDate(format);
    }

    public String getLikesString() {
        MyNumber number = new MyNumber(mLikes);
        return number.getKM_Advanced();
    }

    public String getCommentsString() {
        MyNumber number = new MyNumber(mComments.size());
        return number.getKM();
    }
}

