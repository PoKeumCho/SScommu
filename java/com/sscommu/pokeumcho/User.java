package com.sscommu.pokeumcho;

import org.json.JSONException;
import org.json.JSONObject;

public class User {

    public static final String JSON_ID = "id";
    public static final String JSON_PW = "pw";
    public static final String JSON_ISSUNGSHIN = "issungshin";
    public static final String JSON_NICKNAME = "nickname";
    public static final String JSON_ACCOUNT_IMG_ID = "accountimgid";

    public static final int[] ACCOUNT_IMAGES = {
            R.drawable.account_img_default,
            R.drawable.account_img_student,
            R.drawable.account_img_rest,
            R.drawable.account_img_graduate,
            R.drawable.account_img_worker
    };

    public static final String[] ACCOUNT_IMAGES_NAME = {
            "기본 이미지",
            "재학생",
            "휴학생 / 백수",
            "졸업생",
            "직장인"
    };


    private String mId;
    private String mPw;         // PASSWORD_BCRYPT hash
    private boolean mIsSungshin;
    private String mNickname;
    private int mAccountImgId;
    private int mGeneralCategoryCount;
    private int mGeneralCount;

    /** Constructor */

    // Empty default Constructor
    public User() {}

    public User(String id, String pw) {
        mId = id;
        mPw = pw;
    }

    // Only used when new is called with a JSONObject
    public User(JSONObject jo) throws JSONException {

        mId = jo.getString(JSON_ID);
        mPw = jo.getString(JSON_PW);

    }

    /** Getter & Setter */
    public String getId() { return mId; }
    public void setId(String id) { mId = id; }

    public String getPw() { return mPw; }
    public void setPw(String pw) { mPw = pw; }

    public boolean isSungshin() { return mIsSungshin; }
    public void setIsSungshin(boolean isSungshin) { mIsSungshin = isSungshin; }

    public String getNickname() { return mNickname; }
    public void setNickname(String nickname) { mNickname = nickname; }

    public int getAccountImgId() { return mAccountImgId; }
    public void setAccountImgId(int accountImgId) { mAccountImgId = accountImgId; }

    public int getAccountImgIndex() {
        return getAccountImgId() - 1;   // 1부터 시작하므로,
    }
    public int getAccountImg() { return ACCOUNT_IMAGES[getAccountImgIndex()]; }



    public JSONObject convertToJSON() throws JSONException {

        JSONObject jo = new JSONObject();

        jo.put(JSON_ID, mId);
        jo.put(JSON_PW, mPw);

        return jo;
    }
}
