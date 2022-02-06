package com.sscommu.pokeumcho;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationBarView.OnItemSelectedListener {

    private JSONSerializer mSerializer;
    private User mUser;

    // Can be accessed and edited by all Activity
    private SharedPreferences mPrefs;
    private SharedPreferences.Editor mEditor;

    private Activity mMainActivity;

    private onKeyBackPressedListener mOnKeyBackPressedListener;

    private Fragment mActivateFragment;
    private HomeMainFragment homeMainFragment;
    private GeneralMainFragment generalMainFragment;
    private ScheduleMainFragment scheduleMainFragment;
    private TradeMainFragment tradeMainFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.app_title);

        mActivateFragment = null;
        homeMainFragment = null;
        generalMainFragment = null;
        scheduleMainFragment = null;
        tradeMainFragment = null;

        /** 처음 화면에 HomeMainFragment를 출력한다. */
        // Get a fragment manager
        FragmentManager fragmentManager = getSupportFragmentManager();
        // Create a new fragment using the manager
        mActivateFragment = fragmentManager.findFragmentById(R.id.fragmentHolder);
        // Check the fragment has not already been initialized
        if (mActivateFragment == null) {
            // Initialize the fragment based on our HomeMainFragment
            homeMainFragment = new HomeMainFragment();

            fragmentManager.beginTransaction()
                    .add(R.id.fragmentHolder, homeMainFragment, "home")
                    .commit();

            mActivateFragment = homeMainFragment;
        }

        /** 네비게이션 바 (하단메뉴) 기능 구현 */
        BottomNavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setOnItemSelectedListener(this);

        mSerializer = new JSONSerializer(getApplicationContext());

        mPrefs = getSharedPreferences("SScommu", MODE_PRIVATE);
        mEditor = mPrefs.edit();

        mMainActivity = this;

        /** Login */
        // 저장된 User 정보를 불러온다.
        loadUser();
        // 로그인 성공 여부를 확인한다.
        if (loginHandler()) {
            // 로그인 후 타이틀 바에 프로필 이미지와 닉네임을 출력한다.
            setUserTitleBar();
        }
    }

    public void setOnKeyBackPressedListener(onKeyBackPressedListener listener) {
        mOnKeyBackPressedListener = listener;
    }

    @Override
    public void onBackPressed() {

        if (mOnKeyBackPressedListener != null)
            mOnKeyBackPressedListener.onBack();
        else
            super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // User 정보를 저장한다.
        if (mUser != null) { saveUser(); }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        if (intent.getStringExtra("ACTIVITY_FROM") != null) {
            String activityFrom = intent.getStringExtra("ACTIVITY_FROM");
            switch (activityFrom) {
                case "LoginActivity":
                    loadUser(); // 저장된 User 정보를 불러온다.

                    mUser.setIsSungshin(intent.getBooleanExtra("USER_IS_SUNGSHIN", true));
                    mUser.setNickname(intent.getStringExtra("USER_NICKNAME"));
                    mUser.setAccountImgId(intent.getIntExtra("USER_ACCOUNT_IMG_ID", 1));

                    setUserTitleBar();  // 타이틀 바에 프로필 이미지와 닉네임을 출력한다.
                    break;
            }
        }
    }

    /**
     *  Lag when switching tabs in BottomNavigationView - Fixed
     *  [참고] https://stackoverflow.com/questions/60403240/lag-when-switching-tabs-in-bottomnavigationview
     *
     *  replace(remove All and add) -> hide/show
     *  (If the view is "heavy" I think the hide/show should be used.)
     */
    private void addAllFragment() {

        // Get a fragment manager
        FragmentManager fragmentManager = getSupportFragmentManager();

        // Send data from activity to fragment
        Bundle bundle = new Bundle();
        bundle.putString("USER_ID", mUser.getId());

        generalMainFragment = new GeneralMainFragment();
        scheduleMainFragment = new ScheduleMainFragment();
        tradeMainFragment = new TradeMainFragment();

        generalMainFragment.setArguments(bundle);
        scheduleMainFragment.setArguments(bundle);
        tradeMainFragment.setArguments(bundle);

        fragmentManager.beginTransaction()
                .add(R.id.fragmentHolder, generalMainFragment, "general")
                .hide(generalMainFragment).commit();

        fragmentManager.beginTransaction()
                .add(R.id.fragmentHolder, scheduleMainFragment, "schedule")
                .hide(scheduleMainFragment).commit();

        fragmentManager.beginTransaction()
                .add(R.id.fragmentHolder, tradeMainFragment, "trade")
                .hide(tradeMainFragment).commit();
    }


    /**
     *  네비게이션 바 (하단메뉴) 기능 구현
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        // Create a transaction
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Fragment fragment = null;

        switch (item.getItemId()) {

            case R.id.navigation_home:
                fragment = homeMainFragment;
                break;
            case R.id.navigation_general:
                fragment = generalMainFragment;
                break;
            case R.id.navigation_schedule:
                fragment = scheduleMainFragment;
                break;
            case R.id.navigation_trade:
                fragment = tradeMainFragment;
                break;
        }

        if (fragment == null) { mMainActivity.finishAffinity(); }

        // Implement the change
        transaction.hide(mActivateFragment).show(fragment).commit();
        mActivateFragment = fragment;

        return true;
    }

    /**
     *  타이틀 바 (상단메뉴) 기능 구현
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_top_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_chat:
                Intent chatActivityIntent
                        = new Intent(this, ChatActivity.class);
                chatActivityIntent.putExtra("OPPONENT_ID", "");
                startActivity(chatActivityIntent);
                return true;
            case R.id.action_myinfo:
                Intent myInfoActivityIntent
                        = new Intent(this, MyInfoActivity.class);
                startActivity(myInfoActivityIntent);
                return true;
            case R.id.action_logout:
                mUser.setId("");
                mUser.setPw("");
                startLoginActivity();
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


    /** login */

    private void startLoginActivity() {
        // Declare and initialize a new Intent object called loginIntent
        Intent loginIntent = new Intent(this, LoginActivity.class);

        // Switch to the LoginActivity
        startActivity(loginIntent);
        finish();
    }

    private void loadUser() {

        try {
            mUser = mSerializer.loadUser();

            if (mUser == null) { startLoginActivity(); }

            /**
             *  Fragment 에서 mUser 정보에 접근하므로,
             *  mUser 값을 설정한 후 Fragment 객체를 생성한다.
             */
            else if (generalMainFragment == null) { addAllFragment(); }

        } catch (Exception exc) { startLoginActivity(); }
    }

    private void saveUser() {

        try { mSerializer.saveUser(mUser); }
        catch (Exception exc) { }   // Handle exceptions
    }

    private boolean loginHandler() {

        if (mUser == null) { return false; }

        try {
            if (isNetworkAvailable()) {
                AuthenticationNetworkTask authenticationNetworkTask = new AuthenticationNetworkTask();
                String jsonString = authenticationNetworkTask.execute().get();
                return onPostAuthenticationNetworkTask(jsonString);

            } else {    // Network not available

                /** 네트워크에 연결되어 있지 않은 경우 App을 종료한다. */
                AlertDialog.Builder networkDialog
                        = new AlertDialog.Builder(MainActivity.this);
                networkDialog.setMessage(R.string.network_not_available)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mMainActivity.finishAffinity();
                            }
                        })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                mMainActivity.finishAffinity();
                            }
                        })
                        .show();
            }
        } catch (Exception exc) { startLoginActivity(); }

        return false;
    }

    public class AuthenticationNetworkTask
            extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            String result;

            Map<String, String> queryMap = new HashMap();
            queryMap.put("id", mUser.getId());
            queryMap.put("pw", mUser.getPw());

            SimpleHttpJSON simpleHttpJSON = new SimpleHttpJSON("user", queryMap);
            try {
                result = simpleHttpJSON.sendPost();
            } catch (Exception exc) {
                result = "";
            }
            return result;
        }
    }

    private boolean onPostAuthenticationNetworkTask(String jsonString) {

        try {
            // Parsing String with JSONTokener to JSONObject
            JSONTokener tokener = new JSONTokener(jsonString);
            JSONObject response = new JSONObject(tokener);

            if (response.getBoolean("result")) {
                JSONObject user = response.getJSONObject("user");

                mUser.setNickname(user.getString(User.JSON_NICKNAME));
                mUser.setAccountImgId(user.getInt(User.JSON_ACCOUNT_IMG_ID));

                return true;    // Login Success

            } else { startLoginActivity(); }
        } catch (JSONException exc) { startLoginActivity(); }

        return false;   // Login Fail
    }

    /**
     *  타이틀 바에 프로필 이미지와 닉네임을 출력한다.
     */
    private void setUserTitleBar() {
        try {
            setUserTitleBarInnerCode();
        } catch (Exception exc) {
            // Handle exceptions
        } finally {

            // Persisting data with SharedPreferences
            // provides access to the data that can be accessed and edited by all Activity class.
            mEditor.putString("USER_ID", mUser.getId());
            mEditor.putString("USER_NICKNAME", mUser.getNickname());
            mEditor.putInt("USER_ACCOUNT_IMG_ID", mUser.getAccountImgId());
            mEditor.commit();
        }
    }
    private void setUserTitleBarInnerCode() throws Exception {

        ActionBar actionBar;
        actionBar = getSupportActionBar();

        // 이미지 크기 조절
        Drawable dr = getResources().getDrawable(mUser.getAccountImg());
        Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
        Drawable d = new BitmapDrawable(getResources(),
                Bitmap.createScaledBitmap(bitmap, 40, 40, false));

        // 이미지 출력
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(d);

        actionBar.setTitle("  " + mUser.getNickname());
    }
}