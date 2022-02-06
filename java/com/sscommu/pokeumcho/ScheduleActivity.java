package com.sscommu.pokeumcho;

/* -------------------------------------------------------------------------------------------
    Copyright 2019 tlaabs

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.tlaabs.timetableview.Schedule;
import com.github.tlaabs.timetableview.TimetableView;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ScheduleActivity extends AppCompatActivity
        implements TimetableView.OnStickerSelectedListener,
                    EndlessNestedScrollListener,
                    AdapterView.OnItemSelectedListener,
                    View.OnClickListener {

    private User mUser;
    private SharedPreferences mPrefs;

    /* 사용자 시간표 정보 */
    private ArrayList<MySchedule> mUserSchedules;
    private ArrayList<CollegeSchedule> mCollegeSchedules;

    private TimetableView timetable;
    private TimetableManager timetableManager;

    private EndlessNestedScrollView endlessNestedScrollView;

    private boolean mIsAddDirectlyActivate;
    private View layoutAddDefault;
    private View layoutAddDirectly;

    /** view_schedule_add_default */

    private Spinner searchOptionSpinner;
    private EditText editTextSearch;
    private int mSelectedOption;
    private String mSearch;

    private final int MAX_DISPLAY = 10;                     // 출력 개수
    private ArrayList<CollegeSchedule> mAllSchedules;       // 전체 시간표 목록
    private ArrayList<CollegeSchedule> mSelectedSchedules;  // 사용자 검색 결과 저장
    private ArrayList<CollegeSchedule> mDisplaySchedules;   // 사용자 UI에 띄울 시간표 목록

    private RecyclerView recyclerView;
    private ScheduleAdapter mAdapter;

    private LinearLayout mSelectedScheduleLayout;

    /** view_schedule_add_directly */

    private EditText editTextClassName;
    private EditText editTextClassInfo;

    private final int INPUT_COUNT = 3;
    private LinearLayout[] classTimeInputLayoutArray;
    private Spinner[] daySpinnerArray;
    private Spinner[] firstPeriodSpinnerArray;
    private Spinner[] lastPeriodSpinnerArray;
    private ImageButton[] btnEraseArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        setTitle("시간표 만들기");

        // To create a back button in the title bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mPrefs = getSharedPreferences("SScommu", MODE_PRIVATE);

        mUser = new User();
        mUser.setId(mPrefs.getString("USER_ID", ""));
        mUser.setNickname(mPrefs.getString("USER_NICKNAME", ""));
        mUser.setAccountImgId(mPrefs.getInt("USER_ACCOUNT_IMG_ID", 0));

        mUserSchedules = new ArrayList<MySchedule>();
        mCollegeSchedules = new ArrayList<CollegeSchedule>();

        mAllSchedules = new ArrayList<CollegeSchedule>();
        mSelectedSchedules = new ArrayList<CollegeSchedule>();
        mDisplaySchedules = new ArrayList<CollegeSchedule>();

        // timeTable UI settings
        timetable = findViewById(R.id.timetable);
        timetable.setOnStickerSelectEventListener(this);
        timetableManager = new TimetableManager(timetable);

        endlessNestedScrollView = findViewById(R.id.endlessNestedScrollView);
        endlessNestedScrollView.setScrollViewListener(this);

        // 사용자 시간표 정보를 가져온다.
        getAndDisplaySchedules();

        // 전체 시간표 목록을 가져온다.
        getAllSchedules();

        loadAddDefaultUI();
        loadAddDirectlyUI();

        setAddDefault();
    }

    @Override
    public void onScrollChanged(
            EndlessNestedScrollView scrollView, int x, int y, int oldX, int oldY) {

        // We take the last son in the scrollview
        View view = scrollView.getChildAt(scrollView.getChildCount() - 1);
        int distanceToEnd = (view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY()));

        // if diff is zero, then the bottom has been reached
        if (distanceToEnd == 0 && !mIsAddDirectlyActivate) {

            int size = mDisplaySchedules.size();
            int max = Math.min(mSelectedSchedules.size(), (size + MAX_DISPLAY));
            for (int i = size; i < max; i++)
                mDisplaySchedules.add(mSelectedSchedules.get(i));
            mAdapter.notifyDataSetChanged();
        }
    }

    private void loadAddDefaultUI() {

        layoutAddDefault = findViewById(R.id.layout_add_default);

        searchOptionSpinner = findViewById(R.id.searchOptionSpinner);
        editTextSearch = findViewById(R.id.editTextSearch);

        searchOptionSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(
                    AdapterView<?> parent, View view, int position, long id) {

                if (mSelectedOption != position) {
                    mSelectedOption = position;
                    mSearch = "";
                    editTextSearch.setText("");
                    setSelectedSchedules();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        // To make keyboard enter button say "Search" and handle its click
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

        mSelectedScheduleLayout = null;

        /* Set RecyclerView */
        recyclerView = findViewById(R.id.recyclerView);

        mAdapter = new ScheduleAdapter(this, mDisplaySchedules);

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

    private void loadAddDirectlyUI() {

        layoutAddDirectly = findViewById(R.id.layout_add_directly);

        editTextClassName = findViewById(R.id.editTextClassName);
        editTextClassInfo = findViewById(R.id.editTextClassInfo);

        classTimeInputLayoutArray = new LinearLayout[INPUT_COUNT];
        classTimeInputLayoutArray[0] = findViewById(R.id.classTimeInputLayout_1);
        classTimeInputLayoutArray[1] = findViewById(R.id.classTimeInputLayout_2);
        classTimeInputLayoutArray[2] = findViewById(R.id.classTimeInputLayout_3);

        daySpinnerArray = new Spinner[INPUT_COUNT];
        daySpinnerArray[0] = findViewById(R.id.daySpinner_1);
        daySpinnerArray[1] = findViewById(R.id.daySpinner_2);
        daySpinnerArray[2] = findViewById(R.id.daySpinner_3);

        firstPeriodSpinnerArray = new Spinner[INPUT_COUNT];
        firstPeriodSpinnerArray[0] = findViewById(R.id.firstPeriodSpinner_1);
        firstPeriodSpinnerArray[1] = findViewById(R.id.firstPeriodSpinner_2);
        firstPeriodSpinnerArray[2] = findViewById(R.id.firstPeriodSpinner_3);

        lastPeriodSpinnerArray = new Spinner[INPUT_COUNT];
        lastPeriodSpinnerArray[0] = findViewById(R.id.lastPeriodSpinner_1);
        lastPeriodSpinnerArray[1] = findViewById(R.id.lastPeriodSpinner_2);
        lastPeriodSpinnerArray[2] = findViewById(R.id.lastPeriodSpinner_3);

        btnEraseArray = new ImageButton[INPUT_COUNT];
        btnEraseArray[0] = findViewById(R.id.btnErase_1);
        btnEraseArray[1] = findViewById(R.id.btnErase_2);
        btnEraseArray[2] = findViewById(R.id.btnErase_3);

        for (int i = 0; i < INPUT_COUNT; i++) {
            //daySpinnerArray[i].setOnItemSelectedListener(this);
            firstPeriodSpinnerArray[i].setOnItemSelectedListener(this);
            lastPeriodSpinnerArray[i].setOnItemSelectedListener(this);
            btnEraseArray[i].setOnClickListener(this);
        }
    }

    /** 데이터 베이스에 저장된 시간표 */
    private void setAddDefault() {

        mIsAddDirectlyActivate = false;
        layoutAddDirectly.setVisibility(View.GONE);
        layoutAddDefault.setVisibility(View.VISIBLE);

        mSelectedOption = searchOptionSpinner.getSelectedItemPosition();

        mSearch = "";
        editTextSearch.setText("");
        setSelectedSchedules();
    }

    /** 사용자가 직접 추가 */
    private void setAddDirectly() {

        mIsAddDirectlyActivate = true;

        layoutAddDefault.setVisibility(View.GONE);
        removePreviewSchedule();

        resetAddDirectlyInput();
        layoutAddDirectly.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (mIsAddDirectlyActivate)
            setAddDefault();
        else
            super.onBackPressed();
    }

    /**
     *  타이틀 바 (상단메뉴) 기능 구현
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.schedule_top_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            // Back button in the title bar
            case android.R.id.home:
                finish();
                return true;

            case R.id.action_add_directly:
                if (!mIsAddDirectlyActivate)
                    setAddDirectly();
                else
                    addInputSchedule();
                return true;
        }
        return false;
    }

    @Override
    public void OnStickerSelected(int idx, ArrayList<Schedule> schedules) {

        AlertDialog.Builder dialog
                = new AlertDialog.Builder(ScheduleActivity.this);
        dialog.setMessage(schedules.get(0).getClassTitle() + " 수업을 시간표에서 삭제하시겠습니까?")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteSchedule(schedules.get(0).getProfessorName(), idx, false);
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void performSearch() {

        String search = editTextSearch.getText().toString().trim();

        editTextSearch.setText(search);
        editTextSearch.setSelection(editTextSearch.getText().length());

        if (search.equals("")) {
            Snackbar.make(findViewById(android.R.id.content),
                    "검색어를 입력해주세요.",
                    Snackbar.LENGTH_SHORT).show();
        } else { hideKeyboard(); }

        mSearch = search;
        setSelectedSchedules();
    }

    /** 내부에서 분류 작업을 처리 */
    private void setSelectedSchedules() {

        // 기존 값 초기화
        mSelectedSchedules.clear();
        mDisplaySchedules.clear();

        for (CollegeSchedule schedule: mAllSchedules) {
            if (mSelectedOption == 0) {   // 교과목명
                if (schedule.isClassNameMatch(mSearch))
                    mSelectedSchedules.add(schedule);
            } else {    // 학수번호
                if (schedule.isClassNumberMatch(mSearch))
                    mSelectedSchedules.add(schedule);
            }
        }

        int max = Math.min(mSelectedSchedules.size(), MAX_DISPLAY);
        for (int i = 0; i < max; i++)
            mDisplaySchedules.add(mSelectedSchedules.get(i));

        mAdapter.notifyDataSetChanged();
    }

    /** 사용자가 입력한 시간표를 추가한다. */
    private void addInputSchedule() {

        String className = editTextClassName.getText().toString().trim();
        String classInfo = editTextClassInfo.getText().toString().trim();

        ArrayList<MySchedule> tempSchedules = new ArrayList<MySchedule>();

        /* 유효성 검증 */
        if (className.equals("")) {
            Snackbar.make(findViewById(android.R.id.content),
                    "과목명을 입력해주세요.",
                    Snackbar.LENGTH_SHORT).show();
            editTextClassName.setText("");
            return;
        }

        for (int i = 0; i < INPUT_COUNT; i++) {
            if (daySpinnerArray[i].getSelectedItemPosition() != 0 &&
                    firstPeriodSpinnerArray[i].getSelectedItemPosition() != 0 &&
                    lastPeriodSpinnerArray[i].getSelectedItemPosition() != 0)

                if (!generateMyScheduleFromSpinner(
                        daySpinnerArray[i].getSelectedItemPosition(),
                        firstPeriodSpinnerArray[i].getSelectedItemPosition(),
                        lastPeriodSpinnerArray[i].getSelectedItemPosition(),
                        tempSchedules)) {

                    Snackbar.make(findViewById(android.R.id.content),
                            "중복되는 시간을 하나로 합쳐주세요.",
                            Snackbar.LENGTH_SHORT).show();
                    return;
                }
        }

        if (tempSchedules.size() == 0) {

            Snackbar.make(findViewById(android.R.id.content),
                    "시간을 입력해 주세요.",
                    Snackbar.LENGTH_SHORT).show();
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tempSchedules.size(); i++) {
            sb.append(tempSchedules.get(i).getClassTime());
            if (i != (tempSchedules.size() - 1))
                sb.append(",");
        }
        String classTime = sb.toString();
        if (!checkAddToTimeTablePossibility(classTime)) {

            Snackbar.make(findViewById(android.R.id.content),
                    "같은 시간에 다른 수업이 있습니다.",
                    Snackbar.LENGTH_SHORT).show();
            return;
        }

        showDialogAndAddSchedule(new MySchedule(className, classTime, classInfo));
    }

    private void showDialogAndAddSchedule(MySchedule schedule) {

        AlertDialog.Builder dialog
                = new AlertDialog.Builder(ScheduleActivity.this);
        dialog.setMessage("시간표에 추가하시겠습니까?")
                .setPositiveButton("추가하기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mIsAddDirectlyActivate) { resetAddDirectlyInput(); }
                        addSchedule(schedule);
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    /** ScheduleAdapter */
    public void addScheduleClicked(int position) {

        removePreviewSchedule();

        MySchedule schedule = (MySchedule) mDisplaySchedules.get(position);

        if (checkAddToTimeTablePossibility(schedule.getClassTime())) {
            showDialogAndAddSchedule(schedule);
        } else {
            Snackbar.make(findViewById(android.R.id.content),
                    "같은 시간에 다른 수업이 있습니다.",
                    Snackbar.LENGTH_SHORT).show();
        }
    }
    public void showScheduleClicked(int position, LinearLayout layout) {

        removePreviewSchedule();

        if (checkAddToTimeTablePossibility(mDisplaySchedules.get(position).getClassTime())) {
            mSelectedScheduleLayout = layout;
            mSelectedScheduleLayout.setBackgroundColor(getResources().getColor(R.color.pink_pressed));
            timetableManager.addSchedule(mDisplaySchedules.get(position));
        } else {
            Snackbar.make(findViewById(android.R.id.content),
                    "같은 시간에 다른 수업이 있습니다.",
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    /** 시간표 미리보기를 화면에서 제거한다. */
    private void removePreviewSchedule() {

        if (mSelectedScheduleLayout != null) {
            mSelectedScheduleLayout.setBackgroundColor(Color.WHITE);
            refreshTimeTable();
        }
        mSelectedScheduleLayout = null;
    }

    private void refreshTimeTable() {

        timetableManager.clear();

        for (MySchedule schedule: mUserSchedules)
            timetableManager.addSchedule(schedule);
        for (MySchedule schedule: mCollegeSchedules)
            timetableManager.addSchedule(schedule);
    }

    private void getAndDisplaySchedules() {

        if (isNetworkAvailable()) {
            GetScheduleTask getScheduleTask = new GetScheduleTask(
                    mUser.getId(), mUserSchedules, mCollegeSchedules, timetableManager);
            getScheduleTask.execute();
        } else {
            // Handle network not available.
            Toast.makeText(ScheduleActivity.this,
                    R.string.network_not_available,
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void deleteSchedule(String idString, int idx, boolean showErrorMsg) {

        if (isNetworkAvailable()) {

            DeleteScheduleTask deleteScheduleTask
                    = new DeleteScheduleTask(this, mUser.getId(),
                    mUserSchedules, mCollegeSchedules, timetableManager,
                    idString, idx);

            if (deleteScheduleTask.isValid())
                deleteScheduleTask.execute();

        } else {
            // Handle network not available.
            if (showErrorMsg)
                Snackbar.make(findViewById(android.R.id.content),
                        R.string.network_not_available,
                        Snackbar.LENGTH_SHORT).show();
        }
    }

    private void addSchedule(MySchedule schedule) {

        if (isNetworkAvailable()) {

            AddScheduleTask addScheduleTask
                    = new AddScheduleTask(this, mUser.getId(),
                    mUserSchedules, mCollegeSchedules, timetableManager, schedule);

            addScheduleTask.execute();

        } else {
            // Handle network not available.
            Snackbar.make(findViewById(android.R.id.content),
                    R.string.network_not_available,
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /** 전체 시간표 목록을 가져온다. */
    private void getAllSchedules() {

        if (isNetworkAvailable()) {
            try {
                GetAllNetworkTask getAllNetworkTask = new GetAllNetworkTask();
                String jsonString = getAllNetworkTask.execute().get();
                onPostGetAllNetworkTask(jsonString);
            } catch (Exception exc) { } // Handle Exceptions.
        } else { }  // Handle network not available.
    }

    public class GetAllNetworkTask
            extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            String result;
            Map<String, String> queryMap = new HashMap();
            SimpleHttpJSON simpleHttpJSON
                    = new SimpleHttpJSON("allSchedule", queryMap);
            try {
                result = simpleHttpJSON.sendGet();
            } catch (Exception exc) {
                result = "";
            }
            return result;
        }
    }

    private void onPostGetAllNetworkTask(String jsonString) {

        // 기존 값 초기화
        mAllSchedules.clear();

        try {
            // Parsing String with JSONTokener to JSONObject
            JSONTokener tokener = new JSONTokener(jsonString);
            JSONObject response = new JSONObject(tokener);

            if (response.getBoolean("result")) {

                JSONArray jArray = response.getJSONArray("schedules");
                for (int i = 0; i < jArray.length(); i++)
                    mAllSchedules.add(new CollegeSchedule(jArray.getJSONObject(i)));
            } else {
                // Handle false result
            }
        } catch (JSONException exc) {
            // Handle exceptions
        }
    }

    /** 직접 시간표를 추가하는 경우 교시(period)의 순서를 유효하게 설정한다. */
    @Override
    public void onItemSelected(
            AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()){
            case R.id.firstPeriodSpinner_1:
                firstPeriodSpinnerSelected(0, position);
                break;
            case R.id.firstPeriodSpinner_2:
                firstPeriodSpinnerSelected(1, position);
                break;
            case R.id.firstPeriodSpinner_3:
                firstPeriodSpinnerSelected(2, position);
                break;

            case R.id.lastPeriodSpinner_1:
                lastPeriodSpinnerSelected(0, position);
                break;
            case R.id.lastPeriodSpinner_2:
                lastPeriodSpinnerSelected(1, position);
                break;
            case R.id.lastPeriodSpinner_3:
                lastPeriodSpinnerSelected(2, position);
                break;
        }
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) { }
    /* 시작 교시가 마지막 교시보다 큰 경우 */
    private void firstPeriodSpinnerSelected(int index, int position) {

        if (position > lastPeriodSpinnerArray[index].getSelectedItemPosition()) {
            lastPeriodSpinnerArray[index].setSelection(position);
        }
    }
    /* 마지막 교시가 시작 교시보다 작은 경우 */
    private void lastPeriodSpinnerSelected(int index, int position) {

        if (position < firstPeriodSpinnerArray[index].getSelectedItemPosition()) {
            firstPeriodSpinnerArray[index].setSelection(position);
        }
    }

    /** 직접 시간표를 추가하는 경우 입력 필드의 시간이 겹치지 않고 유효한지 확인한다. */
    private boolean generateMyScheduleFromSpinner(
            int day, int first, int last, ArrayList<MySchedule> schedules) {

        StringBuilder sb = new StringBuilder();

        if (day == 1) sb.append("월/");
        else if (day == 2) sb.append("화/");
        else if (day == 3) sb.append("수/");
        else if (day == 4) sb.append("목/");
        else sb.append("금/");
        sb.append(first);
        if (first != last)
            sb.append("-").append(last);

        String classTime = sb.toString();

        if (schedules.size() > 0) {
            try {
                if (checkAddToTimeTablePossibility_Logic(classTime, schedules)) {
                    schedules.add(new MySchedule(classTime));
                    return true;
                } else { return false; }    // 중복되는 경우
            } catch (Exception exc) { return false; }
        } else {
            schedules.add(new MySchedule(classTime));
            return true;
        }
    }

    /** 현재 시간표에 시간이 중복되지 않는지 확인한다. */
    private boolean checkAddToTimeTablePossibility(String classTime) {

        try {
            ArrayList<MySchedule> schedules = new ArrayList<MySchedule>();

            for (MySchedule schedule: mUserSchedules)
                schedules.add(schedule);
            for (MySchedule schedule: mCollegeSchedules)
                schedules.add(schedule);

            return checkAddToTimeTablePossibility_Logic(classTime, schedules);
        } catch (Exception exc) {
            return false;
        }
    }

    /**
     * @param classTime: 추가할 시간표의 시간
     * @param schedules: 사용자의 모든 시간표 목록
     * @return  중복되지 않을 경우 true 반환.
     * @throws Exception
     */
    private boolean checkAddToTimeTablePossibility_Logic(
            String classTime, ArrayList<MySchedule> schedules) throws Exception {
        String[] classTimes_a = classTime.split(",");

        for (MySchedule schedule: schedules) {
            String[] classTimes_b = schedule.getClassTime().split(",");
            for (int i = 0; i < classTimes_a.length; i++) {
                for (int j = 0; j < classTimes_b.length; j++) {
                    String[] subClassTimes_a = classTimes_a[i].split("/");
                    String[] subClassTimes_b = classTimes_b[j].split("/");

                    // 요일이 같은 경우
                    if (subClassTimes_a[0].equals(subClassTimes_b[0])) {
                        String[] timeStrings_a = subClassTimes_a[1].split("-");
                        String[] timeStrings_b = subClassTimes_b[1].split("-");

                        Integer[] timeInts_a = new Integer[2];
                        Integer[] timeInts_b = new Integer[2];

                        if (timeStrings_a.length == 1) {
                            timeInts_a[0] = Integer.parseInt(timeStrings_a[0]);
                            timeInts_a[1] = Integer.parseInt(timeStrings_a[0]);
                        } else {
                            timeInts_a[0] = Integer.parseInt(timeStrings_a[0]);
                            timeInts_a[1] = Integer.parseInt(timeStrings_a[1]);
                        }

                        if (timeStrings_b.length == 1) {
                            timeInts_b[0] = Integer.parseInt(timeStrings_b[0]);
                            timeInts_b[1] = Integer.parseInt(timeStrings_b[0]);
                        } else {
                            timeInts_b[0] = Integer.parseInt(timeStrings_b[0]);
                            timeInts_b[1] = Integer.parseInt(timeStrings_b[1]);
                        }

                        // 겹치는 경우
                        if (Math.max(timeInts_a[0], timeInts_b[0])
                                <= Math.min(timeInts_a[1], timeInts_b[1])) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /** 시간표 입력 초기화 버튼 구현 */
    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.btnErase_1:
                eraseInputSchedule(0);
                break;
            case R.id.btnErase_2:
                eraseInputSchedule(1);
                break;
            case R.id.btnErase_3:
                eraseInputSchedule(2);
                break;
        }
    }
    private void eraseInputSchedule(int index) {

        daySpinnerArray[index].setSelection(0);
        firstPeriodSpinnerArray[index].setSelection(0);
        lastPeriodSpinnerArray[index].setSelection(0);
    }

    private void resetAddDirectlyInput() {

        editTextClassName.setText("");
        editTextClassInfo.setText("");

        for (int i = 0; i < INPUT_COUNT; i++)
            eraseInputSchedule(i);
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