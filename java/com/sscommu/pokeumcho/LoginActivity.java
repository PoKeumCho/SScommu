package com.sscommu.pokeumcho;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity
        implements View.OnClickListener {

    private JSONSerializer mSerializer;
    private User mUser;

    enum Status { DEFAULT, SUCCESS, LOGIN_ERROR, SUNGSHIN_ERROR, ERROR };
    private Status mResultCode = Status.DEFAULT;

    private EditText editTextId;
    private EditText editTextPw;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Remove title bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        mSerializer = new JSONSerializer(getApplicationContext());

        editTextId = findViewById(R.id.editTextId);
        editTextPw = findViewById(R.id.editTextPw);

        // Handle button click event
        btnLogin = findViewById(R.id.buttonLogin);
        btnLogin.setOnClickListener(this);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        // Exit directly from LoginActivity
        finishAffinity();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.buttonLogin:
                String userId = editTextId.getText().toString();
                String userPw = editTextPw.getText().toString();

                if (userId.length() > 0 && userPw.length() > 0) {

                    mResultCode = Status.DEFAULT;   // 로그인 결과 초기화
                    setResultCode(userId, userPw);  // 로그인 결과를 가져온다.

                    if (mResultCode == Status.SUCCESS) {
                        saveUser();

                        Intent mainIntent = new Intent(this, MainActivity.class);
                        mainIntent.putExtra("ACTIVITY_FROM", "LoginActivity");
                        mainIntent.putExtra("USER_IS_SUNGSHIN", mUser.isSungshin());
                        mainIntent.putExtra("USER_NICKNAME", mUser.getNickname());
                        mainIntent.putExtra("USER_ACCOUNT_IMG_ID", mUser.getAccountImgId());

                        // Starting MainActivity, and clear all stack.
                        finishAffinity();
                        startActivity(mainIntent);

                    } else if (mResultCode == Status.LOGIN_ERROR) {
                        SimpleDialog dialog = new SimpleDialog(
                                this.getString(R.string.login_activity_login_error_msg));
                        dialog.show(getSupportFragmentManager(), "");

                    } else if (mResultCode == Status.SUNGSHIN_ERROR) {
                        SimpleDialog dialog = new SimpleDialog(
                                this.getString(R.string.login_activity_sungshin_error_msg));
                        dialog.show(getSupportFragmentManager(), "");

                    } else if (mResultCode == Status.ERROR) {
                        SimpleDialog dialog = new SimpleDialog(
                                this.getString(R.string.login_activity_error_msg));
                        dialog.show(getSupportFragmentManager(), "");

                    }

                } else {
                    Snackbar.make(findViewById(android.R.id.content),
                            "아이디와 비밀번호를 입력해주세요.",
                            Snackbar.LENGTH_SHORT).show();
                }
                break;
            case R.id.textViewLink:
                // Open a URL in Android's web browser
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://www.sscommu.com"));
                startActivity(browserIntent);
                break;
        }
    }

    private void setResultCode(String userId, String userPw) {

        final int MinIdLength = 5;
        final int MinPwLength = 8;

        if (userId.length() < MinIdLength || userPw.length() < MinPwLength) {
            mResultCode = Status.LOGIN_ERROR;
        } else {
            try {
                NetworkTask networkTask = new NetworkTask(userId, userPw);
                String jsonString = networkTask.execute().get();
                onPostNetworkTask(jsonString);
            } catch (Exception exc) {
                mResultCode = Status.ERROR;
            }
        }
    }

    private void onPostNetworkTask(String jsonString) {

        try {
            // Parsing String with JSONTokener to JSONObject
            JSONTokener tokener = new JSONTokener(jsonString);
            JSONObject response = new JSONObject(tokener);

            if (response.getBoolean("result")) {
                JSONObject user = response.getJSONObject("user");

                mUser = new User(user.getString(User.JSON_ID),
                        user.getString(User.JSON_PW));
                mUser.setIsSungshin(user.getBoolean(User.JSON_ISSUNGSHIN));
                mUser.setNickname(user.getString(User.JSON_NICKNAME));
                mUser.setAccountImgId(user.getInt(User.JSON_ACCOUNT_IMG_ID));

                mResultCode = LoginActivity.Status.SUCCESS;

            } else {
                if (response.getString("msg").equals("SUNGSHIN ERROR")) {
                    mResultCode = LoginActivity.Status.SUNGSHIN_ERROR;
                } else {
                    mResultCode = LoginActivity.Status.LOGIN_ERROR;
                }
            }
        } catch (JSONException exc) {
            mResultCode = LoginActivity.Status.ERROR;
        }
    }

    private void saveUser() {

        try { mSerializer.saveUser(mUser); }
        catch (Exception exc) { }   // Handle exceptions
    }


    /**
     * 네트워크 처리
     */
    public class NetworkTask extends AsyncTask<Void, Void, String> {

        private String mGetId;
        private String mGetPw;

        public NetworkTask(String id, String pw) {
            mGetId = id;
            mGetPw = pw;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String result;

            Map<String, String> queryMap = new HashMap();
            queryMap.put("id", mGetId);
            queryMap.put("pw", mGetPw);

            SimpleHttpJSON simpleHttpJSON = new SimpleHttpJSON("user", queryMap);
            try {
                result = simpleHttpJSON.sendPost();
            } catch (Exception exc) {
                result = "";
            }
            return result;
        }
    }
}