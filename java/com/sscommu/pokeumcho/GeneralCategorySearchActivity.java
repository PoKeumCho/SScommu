package com.sscommu.pokeumcho;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GeneralCategorySearchActivity extends AppCompatActivity
        implements View.OnClickListener {

    private SharedPreferences mPrefs;
    private SharedPreferences.Editor mEditor;

    private String mUserId;
    private ArrayList<GeneralCategory> mSearchCategory;

    private EditText editTextSearch;
    private ImageButton btnSearch;
    private Button btnCreate;
    private ConstraintLayout makeCategoryWrap;
    private TextView txtSearchInfo;
    private LinearLayout resultCategoryWrap;

    private Activity mGeneralCategorySearchActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general_category_search);

        // Remove title bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        mPrefs = getSharedPreferences("SScommu", MODE_PRIVATE);
        mEditor = mPrefs.edit();

        mGeneralCategorySearchActivity = this;

        // 현재 접속한 사용자의 아이디 정보를 가져온다.
        mUserId = mPrefs.getString("USER_ID", "");

        editTextSearch = findViewById(R.id.editTextSearch);
        btnSearch = findViewById(R.id.btnSearch);
        btnCreate = findViewById(R.id.btnCreate);
        makeCategoryWrap = findViewById(R.id.makeCategoryWrap);
        txtSearchInfo = findViewById(R.id.txtSearchInfo);
        resultCategoryWrap = findViewById(R.id.resultCategoryWrap);

        // Handle button click event
        btnSearch.setOnClickListener(this);
        btnCreate.setOnClickListener(this);

        // To make keyboard enter button say "Search" and handle its click
        editTextSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchButtonOnClick();
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
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 검색 결과 메시지를 기본 값으로 설정한다.
        whatToShow(SHOW.DEFAULT);
        txtSearchInfo.setText(R.string.gcs_activity_search_info);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.btnSearch:
                searchButtonOnClick();
                break;
            case R.id.btnCreate:
                DialogCreateCategory dialog = new DialogCreateCategory();
                dialog.sendUserId(mUserId);
                dialog.show(getSupportFragmentManager(), "123");
                break;
        }
    }


    /**
     *  게시판 검색
     */

    private void searchButtonOnClick() {
        String search = editTextSearch.getText().toString();
        search = search.trim();

        if (search.equals("")) {
            whatToShow(SHOW.DEFAULT);
            txtSearchInfo.setText("검색어를 입력해주세요.");
            editTextSearch.setText("");
        } else {
            txtSearchInfo.setText(R.string.gcs_activity_search_info);

            try {
                if (isNetworkAvailable()) {
                    SearchGeneralCategoryNetworkTask searchGeneralCategoryNetworkTask
                            = new SearchGeneralCategoryNetworkTask(search);
                    String jsonString = searchGeneralCategoryNetworkTask.execute().get();
                    onPostSearchGeneralCategoryNetworkTask(jsonString);

                    if (mSearchCategory.size() > 0) {   /* 검색 결과가 존재하는 경우 */

                        whatToShow(SHOW.RESULT);

                        // 기존에 검색된 게시판을 화면에서 제거한다.
                        resultCategoryWrap.removeAllViews();

                        // 새로 검색된 게시판을 화면에 출력한다.
                        for (GeneralCategory category: mSearchCategory) {
                            resultCategoryWrap.addView(
                                    createGeneralCategoryClickable(this, category));
                        }
                    } else {
                        whatToShow(SHOW.DEFAULT);
                        txtSearchInfo.setText("검색 결과가 존재하지 않습니다.");
                    }
                } else {    // Network not available

                    whatToShow(SHOW.DEFAULT);
                    txtSearchInfo.setText(R.string.network_not_available);
                }
            } catch (Exception exc) {
                // Handle exceptions

                whatToShow(SHOW.DEFAULT);
                txtSearchInfo.setText("오류가 발생했습니다.");
            }
        }
    }

    public class SearchGeneralCategoryNetworkTask
            extends AsyncTask<Void, Void, String> {

        private String mSearch;

        public SearchGeneralCategoryNetworkTask(String search) {
            mSearch = search;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String result;

            Map<String, String> queryMap = new HashMap();
            queryMap.put("id", mUserId);
            queryMap.put("search", mSearch);

            SimpleHttpJSON simpleHttpJSON
                    = new SimpleHttpJSON("searchGeneralCategory", queryMap);
            try {
                result = simpleHttpJSON.sendGet();
            } catch (Exception exc) {
                result = "";
            }
            return result;
        }
    }

    private void onPostSearchGeneralCategoryNetworkTask(String jsonString) {

        /* 기존 값 초기화 */
        mSearchCategory = null;
        mSearchCategory = new ArrayList<GeneralCategory>();

        if (jsonString.equals("")) { return; }
        try {
            // Parsing String with JSONTokener to JSONObject
            JSONTokener tokener = new JSONTokener(jsonString);
            JSONArray jArray = new JSONArray(tokener);

            for (int i = 0; i < jArray.length(); i++) {
                mSearchCategory.add(new GeneralCategory(jArray.getJSONObject(i)));
            }

        } catch (JSONException exc) {
            // Handle exceptions
        }
    }

    /**
     *  즐겨찾기 게시판 UI를 생성한다.
     */
    private LinearLayout createGeneralCategoryClickable(Context context,
                                                        GeneralCategory category) {

        /* Frame Layout */
        LinearLayout frameLayout = new LinearLayout(context);
        LinearLayout.LayoutParams frameLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, // width
                LinearLayout.LayoutParams.WRAP_CONTENT  // height
        );
        frameLayoutParams.setMargins(30, 0, 20, 50);
        frameLayout.setLayoutParams(frameLayoutParams);
        frameLayout.setOrientation(LinearLayout.VERTICAL);
        frameLayout.setClickable(true);
        frameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleCategoryOnClick(view, category);
            }
        });

        /* TextView - Name */
        TextView nameText = new TextView(context);
        nameText.setText(category.getName());
        if (category.isExpel()) {
            nameText.setTextColor(Color.parseColor("#0256B0"));
        } else {
            nameText.setTextColor(Color.BLACK);
        }
        nameText.setTextSize(18);
        nameText.setSingleLine(true);
        nameText.setEllipsize(TextUtils.TruncateAt.END);
        frameLayout.addView(nameText);

        /* TextView - Info */
        TextView infoText = new TextView(context);
        infoText.setText(category.getInfo());
        infoText.setTextSize(12);
        frameLayout.addView(infoText);

        return frameLayout;
    }

    /**
     *  동적으로 생성된 즐겨찾기 게시판 UI의 Click 처리
     */
    private void handleCategoryOnClick(View view, GeneralCategory category) {

        String message = "[" +  category.getName() + "] 게시판을 즐겨찾기 목록에 추가하시겠습니까?";

        AlertDialog.Builder addDialog
                = new AlertDialog.Builder(GeneralCategorySearchActivity.this);
        addDialog.setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        try {
                            if (isNetworkAvailable()) {

                                AddCategoryBookmarkNetworkTask addCategoryBookmarkNetworkTask
                                        = new AddCategoryBookmarkNetworkTask(category.getId());
                                String jsonString = addCategoryBookmarkNetworkTask.execute().get();
                                if (onPostAddCategoryBookmarkNetworkTask(jsonString)) {
                                    /** 즐겨찾기 추가 성공 시 */
                                    view.setVisibility(View.GONE);
                                }

                            } else {
                                // Network not available
                            }
                        } catch (Exception exc) {
                            // Handle exceptions
                        }
                    }
                })
                .setNegativeButton("CANCEL", null)
                .show();
    }

    public class AddCategoryBookmarkNetworkTask
            extends AsyncTask<Void, Void, String> {

        private int mCategoryId;

        public AddCategoryBookmarkNetworkTask(int categoryId) {
            mCategoryId = categoryId;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String result;

            Map<String, String> queryMap = new HashMap();
            queryMap.put("id", mUserId);
            queryMap.put("categoryid", String.valueOf(mCategoryId));

            SimpleHttpJSON simpleHttpJSON
                    = new SimpleHttpJSON("addCategoryBookmark", queryMap);
            try {
                result = simpleHttpJSON.sendPost();
            } catch (Exception exc) {
                result = "";
            }
            return result;
        }
    }

    /** 즐겨찾기 추가 성공 여부를 반환한다. */
    private boolean onPostAddCategoryBookmarkNetworkTask(String jsonString) {

        if (jsonString.equals("")) { return false; }
        try {
            // Parsing String with JSONTokener to JSONObject
            JSONTokener tokener = new JSONTokener(jsonString);
            JSONObject response = new JSONObject(tokener);

            return response.getBoolean("result");
        } catch (JSONException exc) {
            return false;
        }
    }


    enum SHOW { DEFAULT, RESULT };
    private void whatToShow(SHOW show) {
        switch (show) {
            case DEFAULT:
                makeCategoryWrap.setVisibility(View.VISIBLE);
                resultCategoryWrap.setVisibility(View.GONE);
                break;
            case RESULT:
                makeCategoryWrap.setVisibility(View.GONE);
                resultCategoryWrap.setVisibility(View.VISIBLE);
                break;
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}