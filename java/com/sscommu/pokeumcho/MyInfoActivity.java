package com.sscommu.pokeumcho;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class MyInfoActivity extends AppCompatActivity
        implements View.OnClickListener {

    private User mUser;
    private SharedPreferences mPrefs;

    private EditText editTextNickname;
    private TextView leftTxt;
    private TextView imgNameTxt;
    private TextView rightTxt;

    private ViewPager myInfoPager;
    private PagerAdapter pagerAdapter;

    private Button btnOk;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_info);
        setTitle("내 정보 설정");

        // To create a back button in the title bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

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

        editTextNickname = findViewById(R.id.editTextNickname);
        leftTxt = findViewById(R.id.leftTxt);
        imgNameTxt = findViewById(R.id.imgNameTxt);
        rightTxt = findViewById(R.id.rightTxt);

        /* 닉네임 */
        editTextNickname.setText(mUser.getNickname());

        /* 프로필 이미지 */
        myInfoPager = findViewById(R.id.myInfoPager);
        pagerAdapter = new ImagePagerAdapter(MyInfoActivity.this, User.ACCOUNT_IMAGES);
        myInfoPager.setAdapter(pagerAdapter);
        myInfoPager.setCurrentItem(mUser.getAccountImgIndex()); // 위치 설정
        setMyInfoPagerNavigation(myInfoPager.getCurrentItem());
        myInfoPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position,
                                       float positionOffset,
                                       int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setMyInfoPagerNavigation(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        btnOk = findViewById(R.id.btnOk);
        btnOk.setOnClickListener(this);
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

    @Override
    public void onClick(View view) {

        int id = view.getId();

        if (id == R.id.btnOk) {
            handleBtnOkOnClick();
        }
    }
    

    private void setMyInfoPagerNavigation(int position) {

        if (position == 0) {
            leftTxt.setVisibility(View.INVISIBLE);
        } else {
            leftTxt.setVisibility(View.VISIBLE);
        }

        if (position == (User.ACCOUNT_IMAGES.length - 1)) {
            rightTxt.setVisibility(View.INVISIBLE);
        } else {
            rightTxt.setVisibility(View.VISIBLE);
        }

        imgNameTxt.setText(User.ACCOUNT_IMAGES_NAME[position]);
    }
    

    /**
     *  '변경하기' 버튼을 누른 경우 
     */
    private void handleBtnOkOnClick() {
        String nickname = editTextNickname.getText().toString().trim();
        int accountImgId = myInfoPager.getCurrentItem() + 1;
        
        if (nickname.equals("")) {
            Toast.makeText(this,
                    R.string.my_info_activity_nickname_error,
                    Toast.LENGTH_SHORT).show();
            editTextNickname.setText(mUser.getNickname());

        } else if (!isNetworkAvailable()) {
            Toast.makeText(this,
                    R.string.network_not_available,
                    Toast.LENGTH_SHORT).show();
        } else {
            mUser.setNickname(nickname);
            mUser.setAccountImgId(accountImgId);

            try {
                NetworkTask networkTask = new NetworkTask();
                networkTask.execute();
            } catch (Exception exc) {
                Toast.makeText(this,
                        R.string.default_error_message,
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public class NetworkTask
            extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            String result;

            Map<String, String> queryMap = new HashMap();
            queryMap.put("id", mUser.getId());
            queryMap.put("nickname", mUser.getNickname());
            queryMap.put("accountimgid", String.valueOf(mUser.getAccountImgId()));

            SimpleHttpJSON simpleHttpJSON = new SimpleHttpJSON("myInfo", queryMap);
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

            Intent mainIntent
                    = new Intent(MyInfoActivity.this, MainActivity.class);

            // Starting MainActivity, and clear all stack.
            finishAffinity();
            startActivity(mainIntent);
        }
    }
}