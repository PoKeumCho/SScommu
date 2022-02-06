package com.sscommu.pokeumcho;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MyTradeArticleActivity extends AppCompatActivity
        implements EndlessNestedScrollListener {

    private User mUser;
    private SharedPreferences mPrefs;
    private SharedPreferences.Editor mEditor;

    private final int MAX_DISPLAY = 10;             // 출력 개수
    private ArrayList<Trade> mTradeList;            // 전체 데이터
    private ArrayList<Trade> mDisplayTradeList;     // 사용자 UI에 띄울 데이터
    private EndlessNestedScrollView endlessNestedScrollView;

    private RecyclerView recyclerView;
    private TradeAdapter mAdapter;
    private ArrayList<AsyncTask> mAsyncTaskList;

    private int mSelectedTradeIndex;
    private int mChangePriceTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_trade_article);
        setTitle("판매내역");

        mPrefs = getSharedPreferences("SScommu", MODE_PRIVATE);
        mEditor = mPrefs.edit();

        mUser = new User();
        mUser.setId(mPrefs.getString("USER_ID", ""));
        mUser.setNickname(mPrefs.getString("USER_NICKNAME", ""));
        mUser.setAccountImgId(mPrefs.getInt("USER_ACCOUNT_IMG_ID", 0));

        // To create a back button in the title bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        endlessNestedScrollView = findViewById(R.id.endlessNestedScrollView);
        endlessNestedScrollView.setScrollViewListener(this);

        mTradeList = new ArrayList<Trade>();
        mDisplayTradeList = new ArrayList<Trade>();
        loadTradeList();

        int max = Math.min(mTradeList.size(), MAX_DISPLAY);
        for (int i = 0; i < max; i++)
            mDisplayTradeList.add(mTradeList.get(i));

        setRecyclerView();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            // Back button in the title bar
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return false;
    }

    @Override
    public void onBackPressed() {
        stopImageLoad();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        stopImageLoad();
        super.onDestroy();
    }

    private void setRecyclerView() {

        mAsyncTaskList = new ArrayList<AsyncTask>();
        recyclerView = findViewById(R.id.recyclerView);
        mAdapter = new TradeAdapter(
                this, mDisplayTradeList, mAsyncTaskList);

        RecyclerView.LayoutManager layoutManager
                = new LinearLayoutManager(getApplicationContext());

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // Add a neat dividing line between items in the list
        recyclerView.addItemDecoration(
                new DividerItemDecoration(this, LinearLayout.VERTICAL));

        // set the adapter
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onScrollChanged(
            EndlessNestedScrollView scrollView, int x, int y, int oldX, int oldY) {

        View view = scrollView.getChildAt(scrollView.getChildCount() - 1);
        int distanceToEnd = (view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY()));

        // If diff is zero, then the bottom has been reached
        if (distanceToEnd == 0) {

            if (mTradeList.size() > mDisplayTradeList.size()
                    && mAsyncTaskList.size() > 0) {
                Toast.makeText(this, R.string.loading_message,
                        Toast.LENGTH_SHORT).show();
            } else {
                int size = mDisplayTradeList.size();
                int max = Math.min(mTradeList.size(), (size + MAX_DISPLAY));
                for (int i = size; i < max; i++)
                    mDisplayTradeList.add(mTradeList.get(i));
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    private void stopImageLoad() {

        for (AsyncTask asyncTask: mAsyncTaskList)
            asyncTask.cancel(true);
        mAsyncTaskList.clear();
    }

    private void restartImageLoad() { mAdapter.notifyDataSetChanged(); }

    public enum Action { CHANGE_PRICE, DELETE, PULL_UP };
    public void executeAction(Action action, int index) {

        mSelectedTradeIndex = index;

        stopImageLoad();

        switch (action) {
            case CHANGE_PRICE:
                DialogChangePrice dialog =
                        new DialogChangePrice(
                                this, mDisplayTradeList.get(index).getPrice());
                dialog.show(getSupportFragmentManager(), "123");
                break;
            case DELETE:
            case PULL_UP:
                tradeDialogHelper(action);
                break;
        }

        restartImageLoad();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /** 판매내역을 가져온다. */
    private void loadTradeList() {

        if (isNetworkAvailable()) {
            try {
                TradeNetworkTask tradeNetworkTask = new TradeNetworkTask();
                String jsonString = tradeNetworkTask.execute().get();
                onPostTradeNetworkTask(jsonString);
            } catch (Exception exc) { finish(2); }   // Handle Exceptions
        } else { finish(1); }   // Handle network not available.
    }

    public class TradeNetworkTask
            extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            String result;

            Map<String, String> queryMap = new HashMap();
            queryMap.put("id", mUser.getId());

            SimpleHttpJSON simpleHttpJSON
                    = new SimpleHttpJSON("myTradeArticle", queryMap);
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

                JSONArray jArray = response.getJSONArray("trade");
                for (int i = 0; i < jArray.length(); i++) {
                    mTradeList.add(new Trade(jArray.getJSONObject(i)));
                }
            } else { finish(2); }
        } catch (JSONException exc) { finish(2); }   // Handle exceptions
    }


    /**
     *  거래완료 (글 삭제), 판매가격 변경, 끌어올리기 처리 구현 부분
     */

    public void changePrice(int price) {
        mChangePriceTo = price;
        tradeActionHelper(Action.CHANGE_PRICE);
    }

    // Handle delete, pull_up
    private void tradeDialogHelper(Action action) {

        AlertDialog.Builder dialog
                = new AlertDialog.Builder(MyTradeArticleActivity.this);
        if (action == Action.DELETE)
            dialog.setMessage(R.string.trade_delete_msg);
        else if (action == Action.PULL_UP) {
            // 글 작성 후 일주일이 지나지 않은 경우에는 끌어올리기를 실행하지 않도록 설정한다.
            MyDate tradeDate
                    = new MyDate(mDisplayTradeList.get(mSelectedTradeIndex).getDate());
            if (!tradeDate.isWeekPassed()) {
                Snackbar.make(findViewById(android.R.id.content),
                        "끌어올리기는 일주일 간격으로 가능합니다.",
                        Snackbar.LENGTH_SHORT).show();
                return;
            } else
                dialog.setMessage(R.string.trade_pull_up_msg);
        }
        dialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                tradeActionHelper(action);
            }
        }).setNegativeButton("취소", null).show();
    }

    private void tradeActionHelper(Action action) {

        if (isNetworkAvailable()) {
            try {
                TradeActionNetworkTask tradeActionNetworkTask
                        = new TradeActionNetworkTask(action);
                String jsonString = tradeActionNetworkTask.execute().get();
                onPostTradeActionNetworkTask(jsonString, action);
            } catch (Exception exc) {
                // Handle Exceptions.
            }
        } else {
            // Handle network not available.
        }
    }

    public class TradeActionNetworkTask
            extends AsyncTask<Void, Void, String> {

        private String action;
        private int price;

        public TradeActionNetworkTask(Action action) {

            this.price = 0;
            switch (action) {
                case CHANGE_PRICE:
                    this.action = "change_price";
                    this.price = mChangePriceTo;
                    break;
                case DELETE:
                    this.action = "delete";
                    break;
                case PULL_UP:
                    this.action = "pull_up";
                    break;
            }
        }

        @Override
        protected String doInBackground(Void... voids) {
            String result;

            Map<String, String> queryMap = new HashMap();
            queryMap.put("action", action);
            queryMap.put("tradeid",
                    String.valueOf(mDisplayTradeList.get(mSelectedTradeIndex).getId()));
            queryMap.put("price", String.valueOf(price));

            SimpleHttpJSON simpleHttpJSON
                    = new SimpleHttpJSON("myTradeArticleAction", queryMap);
            try {
                result = simpleHttpJSON.sendPost();
            } catch (Exception exc) {
                result = "";
            }
            return result;
        }
    }

    private void onPostTradeActionNetworkTask(String jsonString, Action action) {

        try {
            // Parsing String with JSONTokener to JSONObject
            JSONTokener tokener = new JSONTokener(jsonString);
            JSONObject response = new JSONObject(tokener);

            if (response.getBoolean("result")) {
                switch (action) {
                    case CHANGE_PRICE:
                        mDisplayTradeList.get(mSelectedTradeIndex).setPrice(mChangePriceTo);
                        break;
                    case DELETE:
                        mTradeList.remove(mDisplayTradeList.get(mSelectedTradeIndex));
                        mDisplayTradeList.remove(mSelectedTradeIndex);
                        break;
                    case PULL_UP:
                        Trade trade = new Trade(mDisplayTradeList.get(mSelectedTradeIndex));
                        String date = response.getString("date");
                        trade.setDate(date);

                        mTradeList.remove(mDisplayTradeList.get(mSelectedTradeIndex));
                        mDisplayTradeList.remove(mSelectedTradeIndex);

                        mTradeList.add(0, trade);
                        mDisplayTradeList.add(0, trade);
                        break;
                }
                mAdapter.notifyDataSetChanged();
                mEditor.putBoolean("TRADE_CHANGED", true).commit();
            } else {
                // Handle false result
            }
        } catch (JSONException exc) {
            // Handle exceptions
        }
    }


    /**
     * @param option: [1] 네트워크 연결 오류 / [2] 그 외 오류
     */
    private void finish(int option) {

        if (option == 1) {  // 네트워크 연결 오류
            Toast.makeText(MyTradeArticleActivity.this,
                    R.string.network_not_available, Toast.LENGTH_SHORT).show();
        } else if (option == 2) {   // 그 외 오류
            Toast.makeText(MyTradeArticleActivity.this,
                    R.string.default_error_message, Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}