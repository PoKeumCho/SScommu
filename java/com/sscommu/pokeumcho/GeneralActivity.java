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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GeneralActivity extends AppCompatActivity
        implements SwipeRefreshLayout.OnRefreshListener {

    private boolean mSearchActivate;
    private String mCurrentSearchString;

    private User mUser;
    private SharedPreferences mPrefs;

    private GeneralCategory mGeneralCategory;
    private ArrayList<General> mGeneralList;

    private LinearLayout searchWrap;
    private EditText editTextSearch;
    private ImageButton btnSearch;

    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private GeneralAdapter mAdapter;

    ActivityResultLauncher<Intent> mPostRefreshLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general);

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
        int categoryId = intent.getIntExtra("CATEGORY_ID", 0);
        if (categoryId == 0) {
            finish();
        }

        mGeneralCategory = new GeneralCategory();
        mGeneralCategory.setId(categoryId);
        mGeneralCategory.setUserId(intent.getStringExtra("CATEGORY_USERID"));
        mGeneralCategory.setName(intent.getStringExtra("CATEGORY_NAME"));
        mGeneralCategory.setInfo(intent.getStringExtra("CATEGORY_INFO"));

        setTitle(mGeneralCategory.getName());

        // To create a back button in the title bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startEditGeneralActivity();
            }
        });

        searchWrap = findViewById(R.id.searchWrap);
        editTextSearch = findViewById(R.id.editTextSearch);
        btnSearch = findViewById(R.id.btnSearch);

        editTextSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch();
                    return true;
                }
                return false;
            }
        });

        editTextSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    btnSearch.setImageResource(R.drawable.cancel_icon);
                    btnSearch.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) { editTextSearch.setText(""); }
                    });
                }
            }});

        /** '??????' ????????? ?????? ?????? */
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performSearch();
            }
        });

        /** ????????? ?????? ????????? ?????????. */
        mSearchActivate = false;
        searchWrap.setVisibility(View.GONE);
        mCurrentSearchString = "";
        editTextSearch.setText("");

        /** RecyclerView */
        mGeneralList = new ArrayList<General>();
        loadGeneralList("default", null);
        setRecyclerView();

        /**
         *  RecyclerView Pull-To-Refresh
         *  [??????] developer.android.com/jetpack/androidx/releases/swiperefreshlayout
         */
        refreshLayout = findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(this);


        /**
         *  ?????? Activity ?????? GeneralActivity ??? finish() ?????? ??????????????? ????????? ??????.
         */
        mPostRefreshLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_CANCELED) {
                            Refresh();
                        }
                    }
                });
    }

    private void performSearch() {

        String search = editTextSearch.getText().toString().trim();
        mCurrentSearchString = search;

        if (search.equals("")) {
            Snackbar.make(findViewById(android.R.id.content),
                    "???????????? ??????????????????.",
                    Snackbar.LENGTH_SHORT).show();
            editTextSearch.setText(search);
        } else {
            loadGeneralList("search", search);
            mAdapter.notifyDataSetChanged();
            hideKeyboard();
        }
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

        viewGeneralIntent.putExtra("CATEGORY_ID", mGeneralCategory.getId());
        viewGeneralIntent.putExtra("CATEGORY_NAME", mGeneralCategory.getName());

        General.putIntentData(viewGeneralIntent, mGeneralList.get(generalToShow));

        mPostRefreshLauncher.launch(viewGeneralIntent);
    }

    @Override
    public void onBackPressed() {

        if (mSearchActivate) {
            searchDeactivate();
        } else {
            super.onBackPressed();
        }
    }

    /**
     *  ???????????? ??????
     */
    private void Refresh() {

        if (mSearchActivate) {
            if (mCurrentSearchString.equals("")) {
                searchDeactivate();
            } else {
                editTextSearch.setText(mCurrentSearchString);

                // Place cursor at the end of text in EditText
                editTextSearch.setSelection(mCurrentSearchString.length());

                loadGeneralList("search", mCurrentSearchString);
                mAdapter.notifyDataSetChanged();
            }
        } else {
            loadGeneralList("default", null);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onRefresh() {

        Refresh();

        // ?????? ??? ??????
        refreshLayout.setRefreshing(false);
    }

    /** ????????? ?????? ????????? ?????????. */
    private void searchDeactivate() {

        mSearchActivate = false;
        mCurrentSearchString = "";
        editTextSearch.setText("");

        searchWrap.setVisibility(View.GONE);

        // ????????? ???????????? ???????????? ?????? ???????????? ????????????.
        loadGeneralList("default", null);
        mAdapter.notifyDataSetChanged();
    }

    /**
     *  ????????? ??? (????????????) ?????? ??????
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.general_top_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            // Back button in the title bar
            case android.R.id.home:
                finish();
                return true;

            case R.id.action_search:
                searchWrap.setVisibility(View.VISIBLE);
                mSearchActivate = true;
                return true;
            case R.id.action_write:
                startEditGeneralActivity();
                return true;
            case R.id.action_delete_category:
                /* ????????? ????????? ???????????? ????????? ????????????. */
                if (mGeneralCategory.getUserId()
                        .equals(mUser.getId())) {

                    Snackbar.make(findViewById(R.id.ConstraintLayout),
                            R.string.general_activity_delete, Snackbar.LENGTH_LONG)
                            .setAction("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    removeCategory();
                                }
                            }).show();

                } else {
                    Snackbar.make(findViewById(R.id.ConstraintLayout),
                            R.string.general_activity_delete_error,
                            Snackbar.LENGTH_SHORT).show();
                }
                return true;
        }

        return false;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     *
     * @param option : "default" or "search"
     * @param search : ?????????
     */
    private void loadGeneralList(String option, String search) {

        if (isNetworkAvailable()) {
            try {
                GeneralNetworkTask generalNetworkTask
                        = new GeneralNetworkTask(option, search);
                String jsonString = generalNetworkTask.execute().get();
                onPostGeneralNetworkTask(jsonString);
            } catch (Exception exc) {
                // Handle Exceptions.
            }
        } else {
            // Handle network not available.
        }
    }

    public class GeneralNetworkTask
            extends AsyncTask<Void, Void, String> {

        private String option;  // default or search
        private String search;  // ?????????

        public GeneralNetworkTask(String option, String search) {
            this.option = option;
            this.search = search;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String result;

            Map<String, String> queryMap = new HashMap();
            queryMap.put("id", mUser.getId());
            queryMap.put("categoryid", String.valueOf(mGeneralCategory.getId()));
            queryMap.put("option", option);
            if (option.equals("search")) {
                queryMap.put("search", search);
            }

            SimpleHttpJSON simpleHttpJSON = new SimpleHttpJSON("general", queryMap);
            try {
                result = simpleHttpJSON.sendPost();
            } catch (Exception exc) {
                result = "";
            }
            return result;
        }
    }

    private void onPostGeneralNetworkTask(String jsonString) {

        /**
         *  [ ?????? ??? ????????? ]
         *
         *  new ???????????? ?????? ????????? ????????????, ????????? ??????????????? ???????????????,
         *  ?????? ???????????? ?????? ?????????, ????????? ???????????? ???????????? ???????????? ????????????.
         */
        mGeneralList.clear();

        if (jsonString.equals("")) {
            finish();
        }

        try {
            // Parsing String with JSONTokener to JSONObject
            JSONTokener tokener = new JSONTokener(jsonString);
            JSONObject response = new JSONObject(tokener);

            if (response.getBoolean("result")) {
                JSONObject category = response.getJSONObject("category");

                mGeneralCategory = null;
                mGeneralCategory = new GeneralCategory(category);

                JSONArray jArray = response.getJSONArray("general");
                for (int i = 0; i < jArray.length(); i++) {
                    mGeneralList.add(new General(jArray.getJSONObject(i)));
                }
            } else {    // ????????? ???????????? ???????????? ??????
                finish();
            }
        } catch (JSONException exc) {
            // Handle exceptions
            finish();
        }
    }


    private void removeCategory() {

        if (isNetworkAvailable()) {
            try {
                RemoveCategoryNetworkTask removeCategoryNetworkTask
                        = new RemoveCategoryNetworkTask();
                removeCategoryNetworkTask.execute();
            } catch (Exception exc) {
                // Handle Exceptions.
            }
        } else {
            // Handle network not available.
        }
    }

    public class RemoveCategoryNetworkTask
            extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            String result;

            Map<String, String> queryMap = new HashMap();
            queryMap.put("categoryid", String.valueOf(mGeneralCategory.getId()));

            SimpleHttpJSON simpleHttpJSON = new SimpleHttpJSON("removeCategory", queryMap);
            try {
                result = simpleHttpJSON.sendPost();
            } catch (Exception exc) {
                result = "";
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            finish();
        }
    }

    private void startEditGeneralActivity() {

        Intent editGeneralIntent = new Intent(this, EditGeneralActivity.class);

        editGeneralIntent.putExtra("CATEGORY_ID", mGeneralCategory.getId());
        editGeneralIntent.putExtra("CATEGORY_USERID", mGeneralCategory.getUserId());
        editGeneralIntent.putExtra("CATEGORY_NAME", mGeneralCategory.getName());
        editGeneralIntent.putExtra("CATEGORY_INFO", mGeneralCategory.getInfo());

        mPostRefreshLauncher.launch(editGeneralIntent);
    }

    /** Hide keyboard */
    private void hideKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager imm
                    = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }
}