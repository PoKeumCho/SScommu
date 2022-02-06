package com.sscommu.pokeumcho;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TradeMainFragment extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener,
        View.OnClickListener, onKeyBackPressedListener,
        EndlessNestedScrollListener {

    private final int MAX_DISPLAY = 30;     // 출력 개수

    private String mUserId;
    private ArrayList<Trade> mTradeList;                // Http 통신으로 받아온 데이터
    private ArrayList<Trade> mSearchResultTradeList;    // 검색 요건에 해당하는 데이터
    private ArrayList<Trade> mDisplayTradeList;         // 사용자 UI에 띄울 데이터

    private int mSelectedCampus;
    private int mSelectedCategory;
    private String mSearch;

    private LinearLayout myArticleTrade;    // 내 판매 글 clickable layout
    private LinearLayout writeArticleTrade; // 중고거래 글쓰기 clickable layout

    private Spinner campusSpinner;
    private Spinner categorySpinner;
    private String[] mCategory;

    private boolean mIsSearchActivate;
    private Button btnSearchActivate;
    private EditText editTextSearch;

    private RecyclerView recyclerView;
    private TradeAdapter mTradeAdapter;
    private TextView noNetworkTxt;

    private FloatingActionButton fab;

    private SwipeRefreshLayout refreshLayout;

    private EndlessNestedScrollView endlessNestedScrollView;
    private ArrayList<AsyncTask> mAsyncTaskList;

    private SharedPreferences mPrefs;
    private SharedPreferences.Editor mEditor;

    private int mSelectedTradeIndex;

    ActivityResultLauncher<Intent> mPostCheckRemovedLauncher;
    ActivityResultLauncher<Intent> mPostCheckAddLauncher;
    ActivityResultLauncher<Intent> mPostCheckChangeLauncher;

    private boolean mIsActiveFragmentMode;

    private boolean mIsPostOnCreateView;
    private boolean mIsPostLauncher;

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {   /* Do when hidden */

            mIsActiveFragmentMode = false;

            /** onCreateView() 보다 먼저 실행되는 경우에 대비한다. */
            if (mIsPostOnCreateView) {

                if (mIsSearchActivate) { searchDeactivate(); }

                mSelectedCampus = 0;
                mSelectedCategory = 0;
                campusSpinner.setSelection(mSelectedCampus);
                categorySpinner.setSelection(mSelectedCategory);

                clearTask();
                mSearchResultTradeList.clear();
                mDisplayTradeList.clear();
            }
        } else {    /* Do when show */

            mIsActiveFragmentMode = true;

            Reselect();
            endlessNestedScrollView.smoothScrollTo(0,0);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mIsActiveFragmentMode && !mIsPostLauncher) { resumeTask(); }

        if (mIsPostLauncher) { mIsPostLauncher = false; }
    }
    @Override
    public void onStop() {
        super.onStop();
        if (mIsActiveFragmentMode) { clearTask(); }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        // Fragment 에서 Back key Event 처리
        ((MainActivity)getActivity()).setOnKeyBackPressedListener(this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // default value
        mSelectedCampus = 0;
        mSelectedCategory = 0;
        mSearch = "";
        mIsSearchActivate = false;
        mIsActiveFragmentMode = false;
        mIsPostOnCreateView = false;
        mIsPostLauncher = false;

        setCategoryArray();

        /**
         *  TradeMainFragment --> ViewTradeActivity 이동 후,
         *  해당 데이터가 삭제된 경우 mDisplayTradeList 를 수정한다.
         */
        mPrefs = getActivity().getSharedPreferences("SScommu", Context.MODE_PRIVATE);
        mEditor = mPrefs.edit();
        mPostCheckRemovedLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_CANCELED) {

                            if (mPrefs.getBoolean("TRADE_REMOVED", false)) {

                                Snackbar.make(getActivity().findViewById(android.R.id.content),
                                        "게시물이 존재하지 않거나 삭제되었습니다.",
                                        Snackbar.LENGTH_SHORT).show();

                                Trade removedTrade = mDisplayTradeList.get(mSelectedTradeIndex);
                                mTradeList.remove(removedTrade);
                                mSearchResultTradeList.remove(removedTrade);
                                mDisplayTradeList.remove(mSelectedTradeIndex);
                                mTradeAdapter.notifyDataSetChanged();
                            } else { resumeTask(); }

                            mEditor.putBoolean("TRADE_REMOVED", false).commit();
                        }
                    }
                });

        /** TradeMainFragment --> EditTradeActivity 이동 후, 데이터가 추가된 경우 */
        mPostCheckAddLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_CANCELED) {

                            if (mPrefs.getBoolean("TRADE_ADD_SUCCESS", false)) {
                                tradeAddProcess();
                                Reselect();
                            } else { resumeTask(); }

                            mEditor.putBoolean("TRADE_ADD_SUCCESS", false).commit();
                        }
                    }
                });

        /** TradeMainFragment --> MyTradeArticleActivity 이동 후, 데이터가 변경된 경우 */
        mPostCheckChangeLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_CANCELED) {

                            if (mPrefs.getBoolean("TRADE_CHANGED", false)) {
                                notifyChangeNotCommitted();
                            }
                            // 중간에 이미지 로드를 중단한 경우 처리
                            resumeTask();
                            mEditor.putBoolean("TRADE_CHANGED", false).commit();
                        }
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Data which sent from activity
        mUserId = getArguments().getString("USER_ID");

        View view = inflater.inflate(R.layout.content_main_trade,
                container, false);

        myArticleTrade = view.findViewById(R.id.myArticleTrade);
        writeArticleTrade = view.findViewById(R.id.writeArticleTrade);
        campusSpinner = view.findViewById(R.id.campusSpinner);
        categorySpinner = view.findViewById(R.id.categorySpinner);
        btnSearchActivate = view.findViewById(R.id.btnSearchActivate);
        editTextSearch = view.findViewById(R.id.editTextSearch);
        recyclerView = view.findViewById(R.id.recyclerView);
        fab = view.findViewById(R.id.fab);
        refreshLayout = view.findViewById(R.id.refreshLayout);
        noNetworkTxt = view.findViewById(R.id.noNetworkTxt);

        endlessNestedScrollView = view.findViewById(R.id.endlessNestedScrollView);
        endlessNestedScrollView.setScrollViewListener(this);

        myArticleTrade.setOnClickListener(this);
        writeArticleTrade.setOnClickListener(this);
        btnSearchActivate.setOnClickListener(this);
        fab.setOnClickListener(this);

        /**
         *  To make keyboard enter button say "Search" and handle its click
         *  [참고] https://stackoverflow.com/questions/3205339/android-how-to-make-keyboard-enter-button-say-search-and-handle-its-click
         */
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

        // RecyclerView Pull-To-Refresh
        refreshLayout.setOnRefreshListener(this);

        setSpinner();

        mTradeList = new ArrayList<Trade>();
        mSearchResultTradeList = new ArrayList<Trade>();
        mDisplayTradeList = new ArrayList<Trade>();
        loadTradeList();
        setSelectedTradeList();
        setRecyclerView();

        mIsPostOnCreateView = true;

        return view;
    }

    /** EndlessNestedScrollView Scroll Bottom Detect */
    @Override
    public void onScrollChanged(
            EndlessNestedScrollView scrollView, int x, int y, int oldX, int oldY) {

        View view = scrollView.getChildAt(scrollView.getChildCount() - 1);
        int distanceToEnd = (view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY()));

        // If diff is zero, then the bottom has been reached
        if (distanceToEnd == 0) {

            if (mSearchResultTradeList.size() > mDisplayTradeList.size()
                    && mAsyncTaskList.size() > 0) {
                Toast.makeText(getContext(),
                        R.string.loading_message,
                        Toast.LENGTH_SHORT).show();
            } else {
                int size = mDisplayTradeList.size();
                int max = Math.min(mSearchResultTradeList.size(), (size + MAX_DISPLAY));
                for (int i = size; i < max; i++)
                    mDisplayTradeList.add(mSearchResultTradeList.get(i));
                mTradeAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.myArticleTrade:
                clearTask();
                startMyTradeArticleActivity();
                break;
            case R.id.writeArticleTrade:
                clearTask();
                startEditTradeActivity();
                break;
            case R.id.btnSearchActivate:
                searchActivate();
                setFocusOnEditText();
                break;
            case R.id.fab:
                endlessNestedScrollView.smoothScrollTo(0,0);   // Go to top
                break;
        }
    }

    private void setSpinner() {

        ArrayAdapter<String> adapter;
        adapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_dropdown_item, mCategory);
        categorySpinner.setAdapter(adapter);

        /* 캠퍼스 */
        campusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (mSelectedCampus != position) {
                    mSelectedCampus = position;
                    Reselect();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        /* 카테고리 */
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                // 글자 크기 14sp로 설정한다.
                ((TextView) parent.getChildAt(0)).setTextSize(14);

                if (mSelectedCategory != position) {
                    mSelectedCategory = position;
                    Reselect();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void setRecyclerView() {

        mAsyncTaskList = new ArrayList<AsyncTask>();

        mTradeAdapter = new TradeAdapter(
                this, mDisplayTradeList, mAsyncTaskList);

        RecyclerView.LayoutManager layoutManager
                = new LinearLayoutManager(getActivity().getApplicationContext());

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // Add a neat dividing line between items in the list
        recyclerView.addItemDecoration(
                new DividerItemDecoration(getContext(), LinearLayout.VERTICAL));

        // set the adapter
        recyclerView.setAdapter(mTradeAdapter);
    }


    private void Reselect() {

        clearTask();
        setSelectedTradeList();
        mTradeAdapter.notifyDataSetChanged();
    }

    /** 내부에서 분류 작업을 처리 */
    private void setSelectedTradeList() {

        // 기존 값 초기화
        mSearchResultTradeList.clear();
        mDisplayTradeList.clear();

        for (Trade trade: mTradeList) {
            if (trade.isSelectedCampus(mSelectedCampus) &&
                    trade.isSelectedCategory(mSelectedCategory) &&
                    trade.isSearchMatch(mSearch)) {

                mSearchResultTradeList.add(trade);
            }
        }

        int max = Math.min(mSearchResultTradeList.size(), MAX_DISPLAY);
        for (int i = 0; i < max; i++)
            mDisplayTradeList.add(mSearchResultTradeList.get(i));
    }

    private void searchActivate() {

        mIsSearchActivate = true;
        btnSearchActivate.setVisibility(View.GONE);
        editTextSearch.setVisibility(View.VISIBLE);
    }

    private void searchDeactivate() {

        mIsSearchActivate = false;
        btnSearchActivate.setVisibility(View.VISIBLE);
        editTextSearch.setVisibility(View.GONE);
        editTextSearch.setText("");
        mSearch = "";
    }

    private void performSearch() {

        String search = editTextSearch.getText().toString().trim();

        editTextSearch.setText(search);
        editTextSearch.setSelection(editTextSearch.getText().length());

        if (search.equals("")) {
            Snackbar.make(getView(), "검색어를 입력해주세요.",
                    Snackbar.LENGTH_SHORT).show();
        } else {
            mSearch = search;
            Reselect();
            hideKeyboard();
        }
    }

    @Override
    public void onRefresh() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setMessage("새로고침 하시겠습니까?")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (mIsSearchActivate) {
                            editTextSearch.setText(mSearch);
                            editTextSearch.setSelection(editTextSearch.getText().length());
                        }
                        Refresh();
                    }
                })
                .setNegativeButton("취소", null)
                .show();

        refreshLayout.setRefreshing(false); // 로딩 바 제거
    }

    /** 새로고침 처리 */
    private void Refresh() {

        if (mCategory.length == 1) {
            setCategoryArray();
            setSpinner();
        }
        loadTradeList();
        Reselect();
    }

    @Override
    public void onBack() {
        if (mIsSearchActivate) {
            searchDeactivate();
            Reselect();
            endlessNestedScrollView.smoothScrollTo(0,0);
        } else {
            MainActivity activity = (MainActivity) getActivity();
            activity.setOnKeyBackPressedListener(null);
            activity.onBackPressed();
        }
    }

    private void clearTask() {

        for (AsyncTask task: mAsyncTaskList)
            task.cancel(true);

        mAsyncTaskList.clear();
    }

    private void resumeTask() { mTradeAdapter.notifyDataSetChanged(); }

    /** 추가된 데이터를 mTradeList 에 추가한다. */
    private void tradeAddProcess() {

        int id = mPrefs.getInt("TRADE_ADD_ID", 0);
        String category = mPrefs.getString("TRADE_ADD_CATEGORY", "");
        int categoryid = mPrefs.getInt("TRADE_ADD_CATEGORY_ID", 0);
        String title = mPrefs.getString("TRADE_ADD_TITLE", "");
        int price = mPrefs.getInt("TRADE_ADD_PRICE", -1);
        String info = mPrefs.getString("TRADE_ADD_INFO", "");
        String firstImgPath = mPrefs.getString("TRADE_ADD_FIRST_IMG_PATH", "");
        String campus = mPrefs.getString("TRADE_ADD_CAMPUS", "");
        String date = mPrefs.getString("TRADE_ADD_DATE", "");

        if (id != 0 &&
                !category.equals("") &&
                categoryid != 0 &&
                !title.equals("") &&
                price != -1 &&
                !info.equals("") &&
                !firstImgPath.equals("") &&
                !campus.equals("") &&
                !date.equals("")) {

            Trade addTrade = new Trade();

            addTrade.setId(id);
            addTrade.setCategory(category);
            addTrade.setCategoryId(categoryid);
            addTrade.setTitle(title);
            addTrade.setPrice(price);
            addTrade.setInfo(info);
            addTrade.setImgPath(new String[] { firstImgPath });
            addTrade.setCampus(campus);
            addTrade.setDate(date);

            mTradeList.add(0, addTrade);
        }
    }

    private void notifyChangeNotCommitted() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setMessage("변경된 내용은 새로 고침 후에 반영됩니다.")
                .setPositiveButton("확인", null)
                .show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     *  중고거래 목록을 가져온다.
     */
    private void loadTradeList() {

        if (isNetworkAvailable()) {
            noNetworkTxt.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            try {
                TradeNetworkTask tradeNetworkTask = new TradeNetworkTask();
                String jsonString = tradeNetworkTask.execute().get();
                onPostTradeNetworkTask(jsonString);
            } catch (Exception exc) { } // Handle exceptions
        } else {
            recyclerView.setVisibility(View.GONE);
            noNetworkTxt.setVisibility(View.VISIBLE);
        }
    }

    public class TradeNetworkTask
            extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            String result;

            Map<String, String> queryMap = new HashMap();

            /** 모든 데이터를 가져와서 내부에서 분류 작업을 진행하도록 처리 */
            queryMap.put("campus", String.valueOf(0));
            queryMap.put("category", String.valueOf(0));
            queryMap.put("search", "");

            SimpleHttpJSON simpleHttpJSON = new SimpleHttpJSON("trade", queryMap);
            try {
                result = simpleHttpJSON.sendPost();
            } catch (Exception exc) {
                result = "";
            }
            return result;
        }
    }

    private void onPostTradeNetworkTask(String jsonString) {

        // 기존 값 초기화
        mTradeList.clear();

        try {
            // Parsing String with JSONTokener to JSONObject
            JSONTokener tokener = new JSONTokener(jsonString);
            JSONObject response = new JSONObject(tokener);

            if (response.getBoolean("result")) {

                JSONArray jArray = response.getJSONArray("trade");
                for (int i = 0; i < jArray.length(); i++) {
                    mTradeList.add(new Trade(jArray.getJSONObject(i)));
                }
            } else { }  // Handle false result
        } catch (JSONException exc) { } // Handle exceptions
    }

    /** 중고거래 카테고리 목록을 가져온다. */
    private void setCategoryArray() {

        if (isNetworkAvailable()) {
            TradeCategory tradeCategoryHelper = new TradeCategory();
            mCategory = tradeCategoryHelper.getCategoryArray();
        } else {
            Toast.makeText(getContext(),
                    R.string.network_not_available,
                    Toast.LENGTH_SHORT).show();
        }

        if (mCategory == null)
            mCategory = new String[] { "[-- 카테고리 --]" };
    }

    /**
     *  이미지가 모두 로드되기 전에 Activity 를 이동하면 Black Screen 이 발생하므로,
     *  이미지가 모두 로드된 후에만 접근할 수 있도록 제한한다.
     */
    public void showTrade(int tradeToShow) {

        clearTask();

        mSelectedTradeIndex = tradeToShow;

        Intent viewTradeIntent = new Intent(getActivity(), ViewTradeActivity.class);
        viewTradeIntent.putExtra("TRADE_ID", mDisplayTradeList.get(tradeToShow).getId());
        mPostCheckRemovedLauncher.launch(viewTradeIntent);

        mIsPostLauncher = true;
    }

    /** 신고 (expel) 구현 */
    public void expelTrade(int tradeToExpel) {

        final int tradeId = mDisplayTradeList.get(tradeToExpel).getId();

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setMessage(R.string.trade_expel_msg)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        expelTradeProcess(tradeId);
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void expelTradeProcess(int tradeId) {

        clearTask();

        if (isNetworkAvailable()) {
            try {
                TradeExpelNetworkTask tradeExpelNetworkTask = new TradeExpelNetworkTask(tradeId);
                String jsonString = tradeExpelNetworkTask.execute().get();
                onPostTradeExpelNetworkTask(jsonString);
            } catch (Exception exc) { } // Handle exceptions
        } else {
            // Handle network not available
            Snackbar.make(getActivity().findViewById(android.R.id.content),
                    R.string.network_not_available,
                    Snackbar.LENGTH_SHORT).show();
        }

        resumeTask();
    }

    public class TradeExpelNetworkTask
            extends AsyncTask<Void, Void, String> {

        private int tradeId;

        public TradeExpelNetworkTask(int tradeId) {
            this.tradeId = tradeId;
        }

        @Override
        protected String doInBackground(Void... voids) {

            String result;

            Map<String, String> queryMap = new HashMap();
            queryMap.put("id", mUserId);
            queryMap.put("tradeid", String.valueOf(tradeId));

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
                Snackbar.make(getActivity().findViewById(android.R.id.content),
                        R.string.trade_expel_proceed_before_msg,
                        Snackbar.LENGTH_SHORT).show();
            }
        } catch (JSONException exc) {
            // Handle exceptions
        }
    }

    private void startEditTradeActivity() {

        Intent editTradeIntent = new Intent(getActivity(), EditTradeActivity.class);
        mPostCheckAddLauncher.launch(editTradeIntent);

        mIsPostLauncher = true;
    }

    private void startMyTradeArticleActivity() {

        Intent myTradeArticleIntent = new Intent(getActivity(), MyTradeArticleActivity.class);
        mPostCheckChangeLauncher.launch(myTradeArticleIntent);

        mIsPostLauncher = true;
    }

    /** Hide keyboard */
    private void hideKeyboard() {
        if (getActivity().getCurrentFocus() != null) {
            InputMethodManager imm
                    = (InputMethodManager) getActivity()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(
                    getActivity().getCurrentFocus().getWindowToken(), 0);
        }
    }

    /** Set focus on EditText and show keyboard */
    private void setFocusOnEditText() {

        editTextSearch.setFocusable(true);
        editTextSearch.setFocusableInTouchMode(true);
        editTextSearch.requestFocus();
        InputMethodManager imm = (InputMethodManager)
                getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editTextSearch, InputMethodManager.SHOW_IMPLICIT);
    }
}