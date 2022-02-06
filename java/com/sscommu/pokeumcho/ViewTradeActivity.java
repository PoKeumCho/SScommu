package com.sscommu.pokeumcho;

/* -------------------------------------------------------------------------------------------
    [Material View Pager Dots Indicator] 사용 (https://github.com/tommybuonomo/dotsindicator)
    Copyright 2016 Tommy Buonomo

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
------------------------------------------------------------------------------------------- */

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.HashMap;
import java.util.Map;

public class ViewTradeActivity extends AppCompatActivity {

    private final String URL_PREFIX = "https://sscommu.com/file/images/trade/";

    private User mUser;
    private SharedPreferences mPrefs;
    private SharedPreferences.Editor mEditor;

    private Trade mTrade;
    private Bitmap[] mBitmaps;
    private int mImageCount;

    private ViewPager pager;
    private WormDotsIndicator wormDotsIndicator;
    private PagerAdapter adapter;

    private TextView titleTxt;
    private TextView categoryTxt;
    private TextView dateTxt;
    private TextView infoTxt;
    private TextView priceTxt;

    private Button btnExpel;

    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_trade);

        // Remove title bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        mPrefs = getSharedPreferences("SScommu", MODE_PRIVATE);
        mEditor = mPrefs.edit();

        /* Set mUser */
        mUser = new User();
        mUser.setId(mPrefs.getString("USER_ID", ""));
        mUser.setNickname(mPrefs.getString("USER_NICKNAME", ""));
        mUser.setAccountImgId(mPrefs.getInt("USER_ACCOUNT_IMG_ID", 0));
        if (mUser.getId().equals("")
                || mUser.getNickname().equals("")
                || mUser.getAccountImgId() == 0) {
            finish();
        }

        /* Set mTrade */
        Intent intent = getIntent();
        int tradeId = intent.getIntExtra("TRADE_ID", 0);
        if (tradeId == 0) {
            finish();
        } else if (isNetworkAvailable()) {
            try {
                /** mTrade */
                TradeNetworkTask tradeNetworkTask = new TradeNetworkTask(tradeId);
                String jsonString = tradeNetworkTask.execute().get();
                onPostTradeNetworkTask(jsonString);

                /** mBitmaps */
                mImageCount = mTrade.getImgPath().length;
                mBitmaps = new Bitmap[mImageCount];
                BitmapLoadTask bitmapLoadTask
                        = new BitmapLoadTask(mImageCount, URL_PREFIX, mBitmaps, mTrade.getImgPath());
                if (!bitmapLoadTask.execute().get()) {
                    // Fail to load bitmaps.
                    finish();
                }
            } catch (Exception exc) {
                // Handle Exceptions.
                finish();
            }
        } else {
            // Handle network not available.
            Toast.makeText(this,
                    R.string.network_not_available,
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        loadUI();
    }

    private void loadUI() {

        titleTxt = findViewById(R.id.titleTxt);
        categoryTxt = findViewById(R.id.categoryTxt);
        dateTxt = findViewById(R.id.dateTxt);
        infoTxt = findViewById(R.id.infoTxt);
        priceTxt = findViewById(R.id.priceTxt);
        btnExpel = findViewById(R.id.btnExpel);
        fab = findViewById(R.id.fab);

        if (mTrade != null) {
            titleTxt.setText(mTrade.getTitle());
            categoryTxt.setText(mTrade.getCategory());
            categoryTxt.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
            dateTxt.setText(mTrade.getDateLikeEveryTime());
            infoTxt.setText(mTrade.getInfo());
            priceTxt.setText(mTrade.getPriceString());
        }

        if (mBitmaps != null) {
            pager = findViewById(R.id.pager);
            wormDotsIndicator = findViewById(R.id.worm_dots_indicator);
            adapter = new BitmapPagerAdapter(
                    ViewTradeActivity.this, mBitmaps, true);
            ((BitmapPagerAdapter)adapter).setClickable(this);
            pager.setAdapter(adapter);
            wormDotsIndicator.setViewPager(pager);

            if (mImageCount <= 1)
                wormDotsIndicator.setVisibility(View.GONE);
        }

        btnExpel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { expelTrade(); }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { startChat(); }
        });
    }

    /** 채팅 구현 */
    private void startChat() {

        if (!mUser.getId().equals(mTrade.getUserId())) {
            Intent chatActivityIntent
                    = new Intent(this, ChatActivity.class);
            chatActivityIntent.putExtra("OPPONENT_ID", mTrade.getUserId());
            startActivity(chatActivityIntent);
        } else {
            Snackbar.make(findViewById(android.R.id.content),
                    "본인이 작성한 글입니다.", Snackbar.LENGTH_SHORT).show();
        }
    }

    public void viewPagerOnClick(int position) {
        startImageViewActivity(mImageCount, position);
    }

    private void startImageViewActivity(int count, int position) {

        Intent imageViewIntent = new Intent(this, ImageViewActivity.class);

        imageViewIntent.putExtra("IMAGE_COUNT", count);
        imageViewIntent.putExtra("IMAGE_POSITION", position);
        imageViewIntent.putExtra("URL_PREFIX", URL_PREFIX);
        for (int i = 0; i < count; i++) {
            imageViewIntent.putExtra("IMAGE_PATH_" + i, mTrade.getImgPath()[i]);
        }

        startActivity(imageViewIntent);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public class TradeNetworkTask
            extends AsyncTask<Void, Void, String> {

        private int mTradeId;

        public TradeNetworkTask(int tradeId) { mTradeId = tradeId; }

        @Override
        protected String doInBackground(Void... voids) {
            String result;

            Map<String, String> queryMap = new HashMap();
            queryMap.put("tradeid", String.valueOf(mTradeId));

            SimpleHttpJSON simpleHttpJSON
                    = new SimpleHttpJSON("viewTrade", queryMap);
            try {
                result = simpleHttpJSON.sendPost();
            } catch (Exception exc) {
                result = "";
            }
            return result;
        }
    }

    private void onPostTradeNetworkTask(String jsonString) {

        try {
            // Parsing String with JSONTokener to JSONObject
            JSONTokener tokener = new JSONTokener(jsonString);
            JSONObject response = new JSONObject(tokener);

            if (response.getBoolean("result")) {

                JSONObject tradeObj = response.getJSONObject("trade");
                mTrade = new Trade(tradeObj);
            } else {
                if (response.getString("msg").equals("NO TRADE ERROR")) {
                    mEditor.putBoolean("TRADE_REMOVED", true).commit();
                }
                finish();
            }
        } catch (JSONException exc) {
            // Handle exceptions
            finish();
        }
    }

    /** 신고 (expel) 구현 */
    public void expelTrade() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(ViewTradeActivity.this);
        dialog.setMessage(R.string.trade_expel_msg)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        expelTradeProcess();
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void expelTradeProcess() {

        if (isNetworkAvailable()) {
            try {
                TradeExpelNetworkTask tradeExpelNetworkTask = new TradeExpelNetworkTask();
                String jsonString = tradeExpelNetworkTask.execute().get();
                onPostTradeExpelNetworkTask(jsonString);
            } catch (Exception exc) { } // Handle exceptions
        } else {
            // Handle network not available
            Snackbar.make(findViewById(android.R.id.content),
                    R.string.network_not_available,
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    public class TradeExpelNetworkTask
            extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {

            String result;

            Map<String, String> queryMap = new HashMap();
            queryMap.put("id", mUser.getId());
            queryMap.put("tradeid", String.valueOf(mTrade.getId()));

            SimpleHttpJSON simpleHttpJSON
                    = new SimpleHttpJSON("tradeExpel", queryMap);
            try {
                result = simpleHttpJSON.sendPost();
            } catch (Exception exc) {
                result = "";
            }
            return result;
        }
    }

    private void onPostTradeExpelNetworkTask(String jsonString) {

        try {
            // Parsing String with JSONTokener to JSONObject
            JSONTokener tokener = new JSONTokener(jsonString);
            JSONObject response = new JSONObject(tokener);

            if (response.getBoolean("result") &&
                    !response.getBoolean("proceed")) {
                Snackbar.make(findViewById(android.R.id.content),
                        R.string.trade_expel_proceed_before_msg,
                        Snackbar.LENGTH_SHORT).show();
            }
        } catch (JSONException exc) {
            // Handle exceptions
        }
    }
}