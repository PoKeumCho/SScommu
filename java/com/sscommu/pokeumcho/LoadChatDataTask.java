package com.sscommu.pokeumcho;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LoadChatDataTask
        extends AsyncTask<Void, Void, String> {

    public static MyDate LAST_DATE = null;              // 가장 최근에 전송된 채팅 날짜 저장
    public static final int DELAY_MILLISECONDS = 5000;  // 5초 간격

    private boolean mAsync;
    private boolean mContinuous;
    private AsyncManager mAsyncManager;

    private String mUserId;
    private String mOpponentId;

    private ArrayList<ChatData> mChatDataList;
    private ChatDataAdapter mAdapter;

    private ChatRoomActivity mActivity;

    public LoadChatDataTask(ChatRoomActivity activity,
                            boolean async,
                            boolean continuous,
                            AsyncManager asyncManager,
                            String userId,
                            String opponentId,
                            ArrayList<ChatData> chatDataList,
                            ChatDataAdapter adapter) {

        mActivity = activity;

        mAsync = async;
        mContinuous = continuous;
        mAsyncManager = asyncManager;

        mUserId = userId;
        mOpponentId = opponentId;
        mChatDataList = chatDataList;
        mAdapter = adapter;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mAsyncManager.addTask(this);
    }

    @Override
    protected String doInBackground(Void... voids) {

        String result;

        Map<String, String> queryMap = new HashMap();
        queryMap.put("id", mUserId);
        queryMap.put("opponent_id", mOpponentId);

        SimpleHttpJSON simpleHttpJSON
                = new SimpleHttpJSON("loadChatDataList", queryMap);
        try {
            result = simpleHttpJSON.sendPost();
        } catch (Exception exc) {
            result = "";
        }
        return result;
    }

    @Override
    protected void onPostExecute(String jsonString) {
        super.onPostExecute(jsonString);

        int beforeSize = mChatDataList.size();

        if (mAsync)
            onPost(jsonString);
        if (mContinuous) {
            Handler handler = new Handler();
            mAsyncManager.addHandler(handler);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.e("DEBUG", "postDelayed: run()");
                    newTask().execute();
                    mAsyncManager.removeHandler(handler);
                }
            }, DELAY_MILLISECONDS);
        }

        // 새로운 데이터 추가를 알린다.
        if (beforeSize < mChatDataList.size())
            mActivity.chatDataListener(mChatDataList.size() - beforeSize);


        mAsyncManager.removeTask(this);
    }


    public void onPost(String jsonString) {

        try {
            // Parsing String with JSONTokener to JSONObject
            JSONTokener tokener = new JSONTokener(jsonString);
            JSONObject response = new JSONObject(tokener);

            if (response.getBoolean("result")) {

                JSONArray jArray = response.getJSONArray("chat");
                setChatDataList(jArray, mChatDataList.size() - mActivity.getDateCounter());
            }
        } catch (JSONException exc) {
            // Handle exceptions
        }
    }

    private void setChatDataList(JSONArray jArray, int start) throws JSONException {

        // [주의] 채팅 데이터는 삭제가 불가능하다는 점을 이용하여 구현
        for (int i = start; i < jArray.length(); i++) {

            ChatData chat = new ChatData(jArray.getJSONObject(i), mUserId);

            /** 최근 날짜 여부 확인 */
            boolean isNewDate = false;

            if (LAST_DATE == null)
                isNewDate = true;
            else if (!LAST_DATE.compareDate(chat.getDate()))
                isNewDate = true;

            if (isNewDate) {
                LAST_DATE = new MyDate(chat.getDate());
                mChatDataList.add(new ChatData(LAST_DATE.getChatDate(), chat.getReadStatus()));
                mActivity.addDateCounter();
            }

            mChatDataList.add(new ChatData(jArray.getJSONObject(i), mUserId));
        }
    }

    private LoadChatDataTask newTask() {

        return new LoadChatDataTask(mActivity,
                mAsync, mContinuous, mAsyncManager,
                mUserId, mOpponentId,
                mChatDataList, mAdapter);
    }
}
