package com.sscommu.pokeumcho;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MyGeneralArticleActivity extends AppCompatActivity
        implements SwipeRefreshLayout.OnRefreshListener {

    private User mUser;
    private SharedPreferences mPrefs;

    private String mTitle;
    private String mOption;

    private ArrayList<General> mGeneralList;

    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private GeneralAdapter mAdapter;

    ActivityResultLauncher<Intent> mPostRefreshLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_general_article);

        mPrefs = getSharedPreferences("SScommu", MODE_PRIVATE);

        mUser = new User();
        mUser.setId(mPrefs.getString("USER_ID", ""));
        mUser.setNickname(mPrefs.getString("USER_NICKNAME", ""));
        mUser.setAccountImgId(mPrefs.getInt("USER_ACCOUNT_IMG_ID", 0));

        if (mUser.getId().equals("")
                || mUser.getNickname().equals("")
                || mUser.getAccountImgId() == 0) {
            finish();
        }

        Intent intent = getIntent();
        mTitle = intent.getStringExtra("TITLE");

        setTitle(mTitle);

        if (mTitle.equals(getResources()
                .getString(R.string.myarticle_option_general))) {

            mOption = "general";
        } else if (mTitle.equals(getResources()
                .getString(R.string.myarticle_option_comment))) {

            mOption = "comment";
        } else {
            finish();
        }

        // To create a back button in the title bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mPostRefreshLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_CANCELED) {
                            loadGeneralList();
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                });

        /** RecyclerView */
        mGeneralList = new ArrayList<General>();
        loadGeneralList();
        setRecyclerView();

        /** RecyclerView Pull-To-Refresh */
        refreshLayout = findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            // Back button in the title bar
            case android.R.id.home:
                finish();
                return true;
        }

        return false;
    }

    private void setRecyclerView() {

        recyclerView = findViewById(R.id.recyclerView);
        mAdapter = new GeneralAdapter(this, mGeneralList);
        RecyclerView.LayoutManager layoutManager
                = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // Add a neat dividing line between items in the list
        recyclerView.addItemDecoration(
                new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        // set the adapter
        recyclerView.setAdapter(mAdapter);
    }

    public void showGeneral(int generalToShow) {

        Intent viewGeneralIntent = new Intent(this, ViewGeneralActivity.class);

        viewGeneralIntent.putExtra("CATEGORY_ID",
                mGeneralList.get(generalToShow).getCategoryId());
        viewGeneralIntent.putExtra("CATEGORY_NAME",
                mGeneralList.get(generalToShow).getCategoryName());

        General.putIntentData(viewGeneralIntent, mGeneralList.get(generalToShow));

        mPostRefreshLauncher.launch(viewGeneralIntent);
    }

    @Override
    public void onRefresh() {

        loadGeneralList();
        mAdapter.notifyDataSetChanged();

        // 로딩 바 제거
        refreshLayout.setRefreshing(false);
    }

    private void loadGeneralList() {

        if (isNetworkAvailable()) {
            try {
                MyArticleNetworkTask myArticleNetworkTask = new MyArticleNetworkTask();
                String jsonString = myArticleNetworkTask.execute().get();
                onPostMyArticleNetworkTask(jsonString);
            } catch (Exception exc) {
                // Handle Exceptions.
                finish();
            }
        } else {
            // Handle network not available.
            Toast.makeText(this,
                    R.string.network_not_available,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public class MyArticleNetworkTask
            extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            String result;

            Map<String, String> queryMap = new HashMap();
            queryMap.put("id", mUser.getId());
            queryMap.put("option", mOption);

            SimpleHttpJSON simpleHttpJSON
                    = new SimpleHttpJSON("myGeneralArticle", queryMap);
            try {
                result = simpleHttpJSON.sendPost();
            } catch (Exception exc) {
                result = "";
            }
            return result;
        }
    }

    private void onPostMyArticleNetworkTask(String jsonString) {

        /* 기존 값 초기화 */
        mGeneralList.clear();

        if (jsonString.equals("")) {
            finish();
        }

        try {
            // Parsing String with JSONTokener to JSONObject
            JSONTokener tokener = new JSONTokener(jsonString);
            JSONObject response = new JSONObject(tokener);

            if (response.getBoolean("result")) {

                JSONArray jArray = response.getJSONArray("general");
                for (int i = 0; i < jArray.length(); i++) {
                    mGeneralList.add(new General(jArray.getJSONObject(i)));
                }
            } else {
                finish();
            }
        } catch (JSONException exc) {
            // Handle exceptions
            finish();
        }
    }
}