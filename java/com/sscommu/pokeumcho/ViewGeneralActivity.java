package com.sscommu.pokeumcho;

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
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ViewGeneralActivity extends AppCompatActivity
        implements OnKeyboardVisibilityListener, View.OnClickListener {

    private static final String URL_PREFIX = "https://sscommu.com/file/images/general/";

    private User mUser;
    private SharedPreferences mPrefs;

    private int mCategoryId;
    private String mCategoryName;

    private General mGeneral;
    private Bitmap[] mImgBitmaps;
    private String[] mImgPaths;

    private String mText;
    private EditText editTextTextMultiLine;
    private ImageButton btnWrite;

    // content_view_general.xml
    private ImageView accountImageView;
    private TextView nicknameTxt;
    private TextView dateTxt;
    private TextView textTxt;
    private HorizontalScrollView imageScrollView;
    private ImageView[] images;
    private ImageView imageIcon;
    private TextView imgTxt;
    private TextView likesTxt;
    private TextView commentsTxt;
    private Button btnLikes;
    private Button btnDislikes;
    private Button btnExpel;

    private ArrayList<GeneralComment> mGeneralComments;
    private RecyclerView commentRecyclerView;
    private GeneralCommentAdapter mAdapter;

    /** 대댓글 작성 */
    private RelativeLayout mSelectedLayout;
    private int mSelectedCommentId;
    private boolean mIsWriteComment;    // false: 대댓글을 작성하는 경우

    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_general);

        mIsWriteComment = true;     // 댓글을 작성하는 경우로 설정한다.

        /* 배너 광고 */
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // To create a back button in the title bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mPrefs = getSharedPreferences("SScommu", MODE_PRIVATE);

        mUser = new User();
        mUser.setId(mPrefs.getString("USER_ID", ""));
        mUser.setNickname(mPrefs.getString("USER_NICKNAME", ""));
        mUser.setAccountImgId(mPrefs.getInt("USER_ACCOUNT_IMG_ID", 0));

        /* 카테고리 정보 */
        Intent intent = getIntent();
        mCategoryId = intent.getIntExtra("CATEGORY_ID", 0);
        if (mCategoryId == 0) { /** Category Id 값 0은 오류를 나타낸다. */
            finish();
        }
        mCategoryName = intent.getStringExtra("CATEGORY_NAME");

        setTitle(mCategoryName);

        /* 게시글 정보 */
        mGeneral = General.createGeneral(intent);
        if (mGeneral == null) {
            finish();
        }

        editTextTextMultiLine = findViewById(R.id.editTextTextMultiLine);
        btnWrite = findViewById(R.id.btnWrite);
        btnWrite.setOnClickListener(this);

        // content_view_general.xml
        accountImageView = findViewById(R.id.accountImageView);
        nicknameTxt = findViewById(R.id.nicknameTxt);
        dateTxt = findViewById(R.id.dateTxt);
        textTxt = findViewById(R.id.textTxt);
        imageScrollView = findViewById(R.id.imageScrollView);
        images = new ImageView[] {
                findViewById(R.id.imageView_1),
                findViewById(R.id.imageView_2),
                findViewById(R.id.imageView_3),
                findViewById(R.id.imageView_4),
                findViewById(R.id.imageView_5)
        };
        imageIcon = findViewById(R.id.imageIcon);
        imgTxt = findViewById(R.id.imgTxt);
        likesTxt = findViewById(R.id.likesTxt);
        commentsTxt = findViewById(R.id.commentsTxt);
        btnLikes = findViewById(R.id.btnLikes);
        btnDislikes = findViewById(R.id.btnDislikes);
        btnExpel = findViewById(R.id.btnExpel);

        setGeneralContent();

        btnLikes.setOnClickListener(this);
        btnDislikes.setOnClickListener(this);
        btnExpel.setOnClickListener(this);

        /** RecyclerView */
        mGeneralComments = new ArrayList<GeneralComment>();
        loadGeneralComments();
        setRecyclerView();

        setKeyboardVisibilityListener(this);
    }

    private void setRecyclerView() {

        commentRecyclerView = findViewById(R.id.commentRecyclerView);
        mAdapter = new GeneralCommentAdapter(this, mGeneralComments, mUser.getId());
        RecyclerView.LayoutManager layoutManager
                = new LinearLayoutManager(getApplicationContext());
        commentRecyclerView.setLayoutManager(layoutManager);
        commentRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // Add a neat dividing line between items in the list
        commentRecyclerView.addItemDecoration(
                new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        // set the adapter
        commentRecyclerView.setAdapter(mAdapter);

        commentRecyclerView.setNestedScrollingEnabled(false);
    }

    /**
     *  타이틀 바 (상단메뉴) 기능 구현
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.view_general_top_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            // Back button in the title bar
            case android.R.id.home:
                finish();
                return true;

            case R.id.action_refresh:
                refresh();
                return true;
            case R.id.action_chat:
                actionStartChat();
                return true;
            case R.id.action_delete_general:
                if (mUser.getId().equals(mGeneral.getUserId())) {
                       generalViewHandler(
                               getResources().getString(R.string.general_delete_msg),
                               Action.DELETE);
                } else {
                    Snackbar.make(findViewById(android.R.id.content),
                            "삭제가 불가능한 글입니다.", Snackbar.LENGTH_SHORT).show();
                }
                return true;
        }

        return false;
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.btnLikes:
                generalViewHandler(
                        getResources().getString(R.string.general_likes_msg),
                        Action.LIKES);
                break;
            case R.id.btnDislikes:
                generalViewHandler(
                        getResources().getString(R.string.general_dislikes_msg),
                        Action.DISLIKES);
                break;
            case R.id.btnExpel:
                generalViewHandler(
                        getResources().getString(R.string.general_expel_msg),
                        Action.EXPEL);
                break;

            case R.id.btnWrite:
                writeComment();
                break;
        }
    }

    private void generalViewHandler(String message, Action action) {

        AlertDialog.Builder dialog
                = new AlertDialog.Builder(ViewGeneralActivity.this);
        dialog.setMessage(message)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            generalActionHandler(action);
                        } catch (Exception exc) {
                            // Handle exceptions
                        }
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void writeComment() {

        mText = editTextTextMultiLine.getText().toString().trim();

        if (mText.equals("")) {
            Snackbar.make(findViewById(android.R.id.content),
                    "내용을 입력하세요.",
                    Snackbar.LENGTH_SHORT).show();
            editTextTextMultiLine.setText(mText);
        }

        if (isNetworkAvailable()) {
            try {
                /* 댓글을 작성하는 경우 */
                if (mIsWriteComment)
                    generalActionHandler(Action.WRITE);
                /* 대댓글을 작성하는 경우 */
                else {
                    generalCommentActionHandler(mSelectedCommentId, null, Action.WRITE);
                    changeToWriteCommentMode();
                }
            } catch (Exception exc) {
                // Handle exceptions
            }

            editTextTextMultiLine.setText("");
            hideKeyboard();
        } else {
            Snackbar.make(findViewById(android.R.id.content),
                    R.string.network_not_available,
                    Snackbar.LENGTH_SHORT).show();
        }
    }

     /** Hide keyboard */
    private void hideKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager imm
                    = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    /**
     *  게시글 내용을 설정한다.
     */
    private void setGeneralContent() {

        accountImageView.setImageResource(mGeneral.getUserAccountImg());
        nicknameTxt.setText(mGeneral.getUserNickname());
        dateTxt.setText(mGeneral.getFormattedDate());
        textTxt.setText(mGeneral.getText());
        likesTxt.setText(mGeneral.getLikesString());
        commentsTxt.setText(mGeneral.getCommentsString());

        if (mGeneral.getImgCount() > 0) {
            imgTxt.setText(String.valueOf(mGeneral.getImgCount()));
            setGeneralContentImage(mGeneral.getImgCount());
        } else {
            imageViewGone();
            imageIcon.setVisibility(View.GONE);
            imgTxt.setVisibility(View.GONE);
        }
    }

    /**
     *  게시글 이미지를 서버에서 가져와서 화면에 출력한다. 
     */
    private void setGeneralContentImage(int count) {

        mImgBitmaps = new Bitmap[count];
        mImgPaths = new String[count];

        if (isNetworkAvailable()) {
            try {
                ImagePathLoadTask imagePathLoadTask = new ImagePathLoadTask();
                String jsonString = imagePathLoadTask.execute().get();
                onPostImagePathLoadTask(jsonString);

                BitmapLoadTask bitmapLoadTask
                        = new BitmapLoadTask(count, URL_PREFIX, mImgBitmaps, mImgPaths);
                if (!bitmapLoadTask.execute().get()) {  /* 실패한 경우 */
                    failLoadingImage();
                }

                imageViewVisible(count);

            } catch (Exception exc) {
                // Handle Exceptions.
                failLoadingImage();
            }
        } else {
            // Handle network not available.
            Toast.makeText(this,
                    R.string.network_not_available,
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void imageViewGone() {
        imageScrollView.setVisibility(View.GONE);
        for (int i = 0; i < images.length; i++) {
            images[i].setVisibility(View.GONE);
        }
    }

    private void imageViewVisible(final int count) {
        imageScrollView.setVisibility(View.VISIBLE);
        for (int i = 0; i < count; i++) {
            // declared as final because it will be used in an anonymous class
            final int position = i;
            images[i].setVisibility(View.VISIBLE);
            images[i].setImageBitmap(mImgBitmaps[i]);
            images[i].setClipToOutline(true);   // To make an ImageView with rounded corners
            images[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startImageViewActivity(count, position);
                }
            });
        }
    }

    private void startImageViewActivity(int count, int position) {

        Intent imageViewIntent = new Intent(this, ImageViewActivity.class);

        imageViewIntent.putExtra("IMAGE_COUNT", count);
        imageViewIntent.putExtra("IMAGE_POSITION", position);
        imageViewIntent.putExtra("URL_PREFIX", URL_PREFIX);
        for (int i = 0; i < count; i++) {
            imageViewIntent.putExtra("IMAGE_PATH_" + i, mImgPaths[i]);
        }

        startActivity(imageViewIntent);
    }

    /**
     *  새로고침
     *      - 게시글 존재 여부
     *      - 좋아요 수, 댓글 수
     *      - 댓글과 대댓글을 새로 불러온다.
     */
    private void refresh() {

        if (isNetworkAvailable()) {
            try {
                GeneralViewNetworkTask generalViewNetworkTask
                        = new GeneralViewNetworkTask("refresh");
                String jsonString = generalViewNetworkTask.execute().get();
                onPostGeneralView_Refresh(jsonString);
            } catch (Exception exc) {
                // Handle Exceptions.
                failLoadingGeneral(1);
            }
        } else {
            // Handle network not available.
        }

        likesTxt.setText(mGeneral.getLikesString());
        commentsTxt.setText(mGeneral.getCommentsString());

        // 댓글과 대댓글을 새로 불러온다.
        loadGeneralComments();
        mAdapter.notifyDataSetChanged();
    }

    /** 채팅 구현 */
    private void actionStartChat() {

        if (mGeneral.isWithdrawalUser()) {
            showWithdrawalUserMessage();
            return;
        }

        if (!mUser.getId().equals(mGeneral.getUserId())) {
            Intent chatActivityIntent
                    = new Intent(this, ChatActivity.class);
            chatActivityIntent.putExtra("OPPONENT_ID", mGeneral.getUserId());
            startActivity(chatActivityIntent);
        } else {
            Snackbar.make(findViewById(android.R.id.content),
                    "본인이 작성한 글입니다.", Snackbar.LENGTH_SHORT).show();
        }
    }
    public void showWithdrawalUserMessage() {
        Snackbar.make(findViewById(android.R.id.content),
                "회원탈퇴한 사용자입니다.", Snackbar.LENGTH_SHORT).show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void loadGeneralComments() {

        if (isNetworkAvailable()) {
            try {
                GeneralCommentNetworkTask generalCommentNetworkTask
                        = new GeneralCommentNetworkTask();
                String jsonString = generalCommentNetworkTask.execute().get();
                onPostGeneralCommentNetworkTask(jsonString);
            } catch (Exception exc) {
                // Handle Exceptions.
                failLoadingGeneral(1);
            }
        } else {
            // Handle network not available.
            failLoadingGeneral(0);
        }
    }

    public class GeneralCommentNetworkTask
            extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            String result;

            Map<String, String> queryMap = new HashMap();
            queryMap.put("generalid", String.valueOf(mGeneral.getId()));

            SimpleHttpJSON simpleHttpJSON
                    = new SimpleHttpJSON("generalComment", queryMap);
            try {
                result = simpleHttpJSON.sendPost();
            } catch (Exception exc) {
                result = "";
            }
            return result;
        }
    }

    private void onPostGeneralCommentNetworkTask(String jsonString) {

        /** 기존 값 초기화 */
        mGeneralComments.clear();

        if (jsonString.equals("")) {
            failLoadingGeneral(1);
        }

        try {
            // Parsing String with JSONTokener to JSONObject
            JSONTokener tokener = new JSONTokener(jsonString);
            JSONObject response = new JSONObject(tokener);

            if (response.getBoolean("result")) {

                JSONArray jArray = response.getJSONArray("base_comment");
                for (int i = 0; i < jArray.length(); i++) {
                    mGeneralComments.add(new GeneralComment(jArray.getJSONObject(i)));
                }
            } else {
                failLoadingGeneral(1);
            }
        } catch (JSONException exc) {
            // Handle exceptions
            failLoadingGeneral(1);
        }
    }

    /**
     * @param option [0] NETWORK ERROR / [1] LOAD GENERAL ERROR
     */
    private void failLoadingGeneral(int option) {

        AlertDialog.Builder dialog = new AlertDialog.Builder(ViewGeneralActivity.this);

        if (option == 0)
            dialog.setMessage(R.string.network_not_available);
        else if (option == 1)
            dialog.setMessage(R.string.error_loading_general);

        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                })
                .show();
    }

    public class ImagePathLoadTask
            extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            String result;

            Map<String, String> queryMap = new HashMap();
            queryMap.put("generalid", String.valueOf(mGeneral.getId()));

            SimpleHttpJSON simpleHttpJSON
                    = new SimpleHttpJSON("generalImage", queryMap);
            try {
                result = simpleHttpJSON.sendGet();
            } catch (Exception exc) {
                result = "";
            }
            return result;
        }
    }

    private void onPostImagePathLoadTask(String jsonString) {

        if (jsonString.equals("")) {
            failLoadingImage();
        }
        try {
            // Parsing String with JSONTokener to JSONObject
            JSONTokener tokener = new JSONTokener(jsonString);
            JSONObject response = new JSONObject(tokener);

            if (response.getBoolean("result")) {
                JSONObject path = response.getJSONObject("path");

                for (int i = 0; i < mImgPaths.length; i++) {
                    mImgPaths[i] = path.getString("_" + i);
                }
            } else {
                failLoadingImage();
            }
        } catch (JSONException exc) {
            // Handle exceptions
            failLoadingImage();
        }
    }

    private void failLoadingImage() {
        Toast.makeText(this, R.string.error_loading_image, Toast.LENGTH_SHORT).show();
        finish();
    }


    public enum Action { LIKES, DISLIKES, EXPEL, DELETE, WRITE }
    private void generalActionHandler(Action action) throws Exception {

        GeneralViewNetworkTask generalViewNetworkTask;

        switch (action) {
            case LIKES:
                generalViewNetworkTask = new GeneralViewNetworkTask("likes");
                generalViewNetworkTask.execute().get();
                break;
            case DISLIKES:
                generalViewNetworkTask = new GeneralViewNetworkTask("dislikes");
                generalViewNetworkTask.execute().get();
                break;
            case EXPEL:
                generalViewNetworkTask = new GeneralViewNetworkTask("stranger");
                generalViewNetworkTask.execute().get();
                break;
            case DELETE:
                generalViewNetworkTask = new GeneralViewNetworkTask("delete");
                generalViewNetworkTask.execute().get();
                break;
            case WRITE:
                generalViewNetworkTask = new GeneralViewNetworkTask("write_comment");
                generalViewNetworkTask.execute().get();
                break;
        }

        if (action == Action.DELETE)
            finish();
        else
            refresh();
    }
    public void generalCommentActionHandler(
            int generalCommentId, String userId, Action action) throws Exception {

        GeneralViewNetworkTask generalViewNetworkTask;

        switch (action) {
            case LIKES:
                generalViewNetworkTask = new GeneralViewNetworkTask("comment_likes");
                generalViewNetworkTask.setCommentId(generalCommentId);
                generalViewNetworkTask.execute().get();
                break;
            case DISLIKES:
                generalViewNetworkTask = new GeneralViewNetworkTask("comment_dislikes");
                generalViewNetworkTask.setCommentId(generalCommentId);
                generalViewNetworkTask.execute().get();
                break;
            case EXPEL:
                generalViewNetworkTask = new GeneralViewNetworkTask("comment_stranger");
                generalViewNetworkTask.setCommentId(generalCommentId);
                generalViewNetworkTask.setStrangerUserId(userId);
                generalViewNetworkTask.execute().get();
                break;
            case DELETE:
                generalViewNetworkTask = new GeneralViewNetworkTask("comment_delete");
                generalViewNetworkTask.setCommentId(generalCommentId);
                generalViewNetworkTask.execute().get();
                break;
            case WRITE:
                generalViewNetworkTask = new GeneralViewNetworkTask("write_ccomment");
                generalViewNetworkTask.setCommentId(generalCommentId);
                generalViewNetworkTask.execute().get();
                break;
        }

        refresh();
    }

    public class GeneralViewNetworkTask
            extends AsyncTask<Void, Void, String> {

        private String option;
        private int commentId;
        private String strangerUserId;

        public GeneralViewNetworkTask(String option) {
            this.option = option;
        }

        public void setCommentId(int commentId) {
            this.commentId = commentId;
        }

        public void setStrangerUserId(String strangerUserId) {
            this.strangerUserId = strangerUserId;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String result;

            Map<String, String> queryMap = new HashMap();
            queryMap.put("id", mUser.getId());
            queryMap.put("generalid", String.valueOf(mGeneral.getId()));
            queryMap.put("categoryid", String.valueOf(mCategoryId));
            queryMap.put("option", option);
            switch (option) {
                case "stranger":
                    queryMap.put("stranger_userid", mGeneral.getUserId());
                    break;
                case "comment_likes":
                case "comment_dislikes":
                case "comment_delete":
                    queryMap.put("commentid", String.valueOf(commentId));
                    break;
                case "comment_stranger":
                    queryMap.put("commentid", String.valueOf(commentId));
                    queryMap.put("stranger_userid", strangerUserId);
                    break;
                case "write_comment":
                    queryMap.put("text", mText);
                    break;
                case "write_ccomment":
                    queryMap.put("text", mText);
                    queryMap.put("commentid", String.valueOf(commentId));
                    break;
            }

            SimpleHttpJSON simpleHttpJSON
                    = new SimpleHttpJSON("generalView", queryMap);
            try {
                result = simpleHttpJSON.sendPost();
            } catch (Exception exc) {
                result = "";
            }
            return result;
        }
    }

    /** 게시글 새로고침 처리 */
    private void onPostGeneralView_Refresh(String jsonString) {

        if (jsonString.equals("")) {
            failLoadingGeneral(1);
        }
        try {
            // Parsing String with JSONTokener to JSONObject
            JSONTokener tokener = new JSONTokener(jsonString);
            JSONObject response = new JSONObject(tokener);

            if (response.getBoolean("result")) {

                mGeneral.setLikes(response.getInt("likes"));
                mGeneral.setComments(response.getInt("comments"));
            } else {
                failLoadingGeneral(1);
            }
        } catch (JSONException exc) {
            // Handle exceptions
            failLoadingGeneral(1);
        }
    }

    /**
     *  대댓글을 작성하는 경우
     */
    public void generalCommentWriteComment(RelativeLayout layout, int generalCommentId) {

        String text = editTextTextMultiLine.getText().toString().trim();

        /** 작성하던 대댓글이 존재하는 경우 */
        if (!mIsWriteComment && (generalCommentId != mSelectedCommentId)) {
            AlertDialog.Builder dialog
                    = new AlertDialog.Builder(ViewGeneralActivity.this);
            dialog.setMessage(R.string.general_write_ccomment_error_msg)
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            editTextTextMultiLine.setText("");
                            changeToWriteCommentMode();
                            changeToWriteCCommentMode(layout, generalCommentId);
                        }
                    })
                    .setNegativeButton("취소", null)
                    .show();
        } else if (text.equals("")) {
            editTextTextMultiLine.setText("");
            changeToWriteCCommentMode(layout, generalCommentId);
        }
        /** 작성하던 댓글이 존재하는 경우 */
        else {
            if (mIsWriteComment) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(ViewGeneralActivity.this);
                dialog.setMessage(R.string.general_write_comment_error_msg)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                editTextTextMultiLine.setText("");
                                changeToWriteCCommentMode(layout, generalCommentId);
                            }
                        })
                        .setNegativeButton("취소", null)
                        .show();
            }
        }

        setFocusOnEditText();
    }

    /** Set focus on EditText and show keyboard */
    private void setFocusOnEditText() {

        editTextTextMultiLine.setFocusable(true);
        editTextTextMultiLine.setFocusableInTouchMode(true);
        editTextTextMultiLine.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editTextTextMultiLine, InputMethodManager.SHOW_IMPLICIT);
    }

    /* 댓글 작성 모드로 전환한다. */
    private void changeToWriteCommentMode() {
        mIsWriteComment = true;
        editTextTextMultiLine.setHint(R.string.write_general_comment);
        editTextTextMultiLine.setText("");
        if (mSelectedLayout != null)
            mSelectedLayout.setBackgroundColor(Color.WHITE);
    }

    /* 대댓글 작성 모드로 전환한다. */
    private void changeToWriteCCommentMode(RelativeLayout layout, int generalCommentId) {
        mIsWriteComment = false;
        mSelectedLayout = layout;
        mSelectedCommentId = generalCommentId;

        layout.setBackgroundColor(getResources().getColor(R.color.pink_pressed));
        editTextTextMultiLine.setHint(R.string.write_general_ccomment);
    }

    /**
     *  Capture the "keyboard show/hide" event in Android
     *  [참고] https://stackoverflow.com/questions/4312319/how-to-capture-the-virtual-keyboard-show-hide-event-in-android
     */
    private void setKeyboardVisibilityListener(
            final OnKeyboardVisibilityListener onKeyboardVisibilityListener) {

        final View parentView
                = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
        parentView.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                    private boolean alreadyOpen;
                    private final int defaultKeyboardHeightDP = 100;
                    private final int EstimatedKeyboardDP = defaultKeyboardHeightDP +
                            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? 48 : 0);
                    private final Rect rect = new Rect();

                    @Override
                    public void onGlobalLayout() {

                        int estimatedKeyboardHeight
                                = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                    EstimatedKeyboardDP,
                                    parentView.getResources().getDisplayMetrics());
                        parentView.getWindowVisibleDisplayFrame(rect);
                        int heightDiff = parentView.getRootView().getHeight() - (rect.bottom - rect.top);
                        boolean isShown = heightDiff >= estimatedKeyboardHeight;

                        if (isShown == alreadyOpen) {
                            Log.i("Keyboard state", "Ignoring global layout change...");
                            return;
                        }

                        alreadyOpen = isShown;
                        onKeyboardVisibilityListener.onVisibilityChanged(isShown);
                    }
                });
    }

    /** 키보드 상태 변화 처리 부분 */
    @Override
    public void onVisibilityChanged(boolean visible) {

        if (visible) {
            // Keyboard is active
        } else {
            // Keyboard is Inactive
            if (!mIsWriteComment) {

                String text = editTextTextMultiLine.getText().toString().trim();

                if (text.equals("")) {
                    editTextTextMultiLine.setText("");
                    changeToWriteCommentMode();
                } else {    // 작성하던 대댓글이 존재하는 경우
                    AlertDialog.Builder dialog = new AlertDialog.Builder(ViewGeneralActivity.this);
                    dialog.setMessage(R.string.general_write_ccomment_error_msg)
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    editTextTextMultiLine.setText("");
                                    changeToWriteCommentMode();
                                }
                            })
                            .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    setFocusOnEditText();
                                }
                            })
                            .show();
                }
            }
        }
    }
}