package com.sscommu.pokeumcho;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.HashMap;
import java.util.Map;

public class TradeCategory {

    private String[] mCategory;

    /** 중고거래 카테고리 목록을 가져온다. */
    public String[] getCategoryArray() {

        try {
            TradeCategoryNetworkTask tradeCategoryNetworkTask
                    = new TradeCategoryNetworkTask();
            String jsonString = tradeCategoryNetworkTask.execute().get();
            onPostTradeCategoryNetworkTask(jsonString);
        } catch (Exception exc) {
            mCategory = null;
        }

        return mCategory;
    }

    public class TradeCategoryNetworkTask
            extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            String result;

            Map<String, String> queryMap = new HashMap();

            SimpleHttpJSON simpleHttpJSON
                    = new SimpleHttpJSON("tradeCategory", queryMap);
            try {
                result = simpleHttpJSON.sendGet();
            } catch (Exception exc) {
                result = "";
            }
            return result;
        }
    }

    private void onPostTradeCategoryNetworkTask(String jsonString) {

        try {
            // Parsing String with JSONTokener to JSONObject
            JSONTokener tokener = new JSONTokener(jsonString);
            JSONObject response = new JSONObject(tokener);

            if (response.getBoolean("result")) {
                JSONObject category = response.getJSONObject("category");

                mCategory = new String[(category.length() + 1)];
                mCategory[0] = "[-- 카테고리 --]";
                for (int i = 0; i < category.length(); i++)
                    mCategory[(i+1)] = category.getString("_" + i);
            }
        } catch (JSONException exc) {
            mCategory = null;
        }
    }
}
