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

public class LoadChatOpponentTask
        extends AsyncTask<Void, Void, String> {

    public static final int DELAY_MILLISECONDS = 5000;    // 5초 간격

    private boolean mAsync;
    private boolean mContinuous;
    private AsyncManager mAsyncManager;

    private String mUserId;
    private String mOpponentId;

    private ArrayList<ChatOpponent> mOpponentList;
    private ChatOpponentAdapter mAdapter;

    public LoadChatOpponentTask(boolean async,
                                boolean continuous,
                                AsyncManager asyncManager,
                                String userId,
                                String opponentId,
                                ArrayList<ChatOpponent> opponentList,
                                ChatOpponentAdapter adapter) {

        mAsync = async;
        mContinuous = continuous;
        mAsyncManager = asyncManager;

        mUserId = userId;
        mOpponentId = opponentId;
        mOpponentList = opponentList;
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
                = new SimpleHttpJSON("loadChatOpponentList", queryMap);
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

        mAsyncManager.removeTask(this);
    }

    public boolean onPost(String jsonString) {

        try {
            // Parsing String with JSONTokener to JSONObject
            JSONTokener tokener = new JSONTokener(jsonString);
            JSONObject response = new JSONObject(tokener);

            if (response.getBoolean("result")) {

                mOpponentList.clear();  // 기존 값 초기화

                JSONArray jArray = response.getJSONArray("opponents");
                for (int i = 0; i < jArray.length(); i++)
                    mOpponentList.add(new ChatOpponent(jArray.getJSONObject(i)));
                mAdapter.notifyDataSetChanged();

                return response.getBoolean("block");
            }
        } catch (JSONException exc) { } // Handle exceptions

        return true;    // default
    }

    private LoadChatOpponentTask newTask() {

        return new LoadChatOpponentTask(
                mAsync, mContinuous, mAsyncManager,
                mUserId, mOpponentId,
                mOpponentList, mAdapter);
    }
}
