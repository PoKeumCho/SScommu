package com.sscommu.pokeumcho;

import android.graphics.Bitmap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class Trade {

    private static final String JSON_ID = "id";
    private static final String JSON_USER_ID = "userid";
    private static final String JSON_CATEGORY_ID = "categoryid";
    private static final String JSON_CATEGORY = "category";
    private static final String JSON_TITLE = "title";
    private static final String JSON_PRICE = "price";
    private static final String JSON_INFO = "info";
    private static final String JSON_IMG_ID = "imgid";
    private static final String JSON_IMG_PATH = "img_path";
    private static final String JSON_CAMPUS = "campus";
    private static final String JSON_DATE = "date";
    private static final String JSON_EXPEL = "expel";

    private int mId;
    private String mUserId;
    private int mCategoryId;
    private String mCategory;
    private String mTitle;
    private int mPrice;
    private String mInfo;
    private int mImgId;
    private String[] mImgPath;
    private String mCampus;
    private String mDate;
    private int mExpel;

    private Bitmap mRepresentativeBitmap;   // 대표 이미지 (TradeMainFragment 에서 사용)

    /** Constructor */
    // Empty default Constructor
    public Trade() { }

    public Trade(JSONObject jo) throws JSONException {

        mId = jo.getInt(JSON_ID);
        mUserId = jo.getString(JSON_USER_ID);
        mCategoryId = jo.getInt(JSON_CATEGORY_ID);
        mCategory = jo.getString(JSON_CATEGORY);
        mTitle = jo.getString(JSON_TITLE);
        mPrice = jo.getInt(JSON_PRICE);
        mInfo = jo.getString(JSON_INFO);
        mImgId = jo.getInt(JSON_IMG_ID);
        mCampus = jo.getString(JSON_CAMPUS);
        mDate = jo.getString(JSON_DATE);
        mExpel = jo.getInt(JSON_EXPEL);

        JSONArray jArray = jo.getJSONArray(JSON_IMG_PATH);
        mImgPath = new String[jArray.length()];
        for (int i = 0; i < jArray.length(); i++) {
            mImgPath[i] = jArray.getString(i);
        }

        mRepresentativeBitmap = null;
    }

    public Trade(Trade trade) {

        mId = trade.mId;
        mUserId = trade.mUserId;
        mCategoryId = trade.mCategoryId;
        mCategory = trade.mCategory;
        mTitle = trade.mTitle;
        mPrice = trade.mPrice;
        mInfo = trade.mInfo;
        mImgId = trade.mImgId;
        mImgPath = trade.mImgPath;
        mCampus = trade.mCampus;
        mDate = trade.mDate;
        mExpel = trade.mExpel;
        mRepresentativeBitmap = trade.mRepresentativeBitmap;
    }

    /** Getter & Setter */
    public int getId() { return mId; }
    public void setId(int id) { mId = id; }

    public String getUserId() { return mUserId; }
    public void setUserId(String userId) { mUserId = userId; }

    public int getCategoryId() { return mCategoryId; }
    public void setCategoryId(int categoryId) { mCategoryId = categoryId; }

    public String getCategory() { return mCategory; }
    public void setCategory(String category) { mCategory = category; }

    public String getTitle() { return mTitle; }
    public void setTitle(String title) { mTitle = title; }

    public int getPrice() { return mPrice; }
    public void setPrice(int price) { mPrice = price; }

    public String getInfo() { return mInfo; }
    public void setInfo(String info) { mInfo = info; }

    public int getImgId() { return mImgId; }
    public void setImgId(int imgId) { mImgId = imgId; }

    public String[] getImgPath() { return mImgPath; }
    public void setImgPath(String[] imgPath) { mImgPath = imgPath; }

    public String getCampus() { return mCampus; }
    public void setCampus(String campus) { mCampus = campus; }

    public String getDate() { return mDate; }
    public void setDate(String date) { mDate = date; }

    public int getExpel() { return mExpel; }
    public void setExpel(int expel) { mExpel = expel; }

    public Bitmap getRepresentativeBitmap() { return mRepresentativeBitmap; }
    public void setRepresentativeBitmap(Bitmap bitmap) { mRepresentativeBitmap = bitmap; }

    /** Formatted */
    public String getDateLikeEveryTime() {
        MyDate date = new MyDate(mDate);
        return date.getDateLikeEveryTime(new Date());
    }

    public String getCampusString() {
        if (mCampus.equals("S"))
            return "수정";
        else if (mCampus.equals("U"))
            return "운정";
        else
            return "수정•운정";
    }

    public String getPriceString() {
        MyNumber number = new MyNumber(mPrice);
        return number.priceCommaFormatted() + "원";
    }


    /** 분류 작업 관련 함수 정의 */

    /**
     * @param campus : [0] 전체 ; [1] 수정 ; [2] 운정
     */
    public boolean isSelectedCampus(int campus) {

        if (campus == 0)
            return true;
        if (mCampus.equals("B"))
            return true;
        if (campus == 1 && mCampus.equals("S"))
            return true;
        if (campus == 2 && mCampus.equals("U"))
            return true;

        return false;
    }

    public boolean isSelectedCategory(int categoryId) {

        if (categoryId == 0)
            return true;
        else if (mCategoryId == categoryId)
            return true;
        else
            return false;
    }

    public boolean isSearchMatch(String search) {

        if (mTitle.indexOf(search) < 0)
            return false;
        else
            return true;
    }
}
