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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    private User mUser;
    private SharedPreferences mPrefs;

    private String mOpponentId;

    private ArrayList<ChatOpponent> mOpponentList;
    private RecyclerView recyclerView;
    private ChatOpponentAdapter mAdapter;

    private AsyncManager asyncManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setTitle("채팅");

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

        // 채팅 상대를 지정한 경우 (채팅 상대가 존재하지 않으면 빈문자열 전달)
        Intent intent = getIntent();
        mOpponentId = intent.getStringExtra("OPPONENT_ID");

        mOpponentList = new ArrayList<ChatOpponent>();
        asyncManager = new AsyncManager();

        setRecyclerView();
        loadChatOpponent();
    }

    @Override
    protected void onPause() {
        super.onPause();
        asyncManager.clear();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundTask();
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
        mAdapter = new ChatOpponentAdapter(this, mOpponentList);
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

    private void unblockOpponent() {

        asyncManager.clear();

        if (isNetworkAvailable()) {
            try {
                ChatTask chatNetworkTask
                        = new ChatTask(this, mUser.getId(), mOpponentId);
                chatNetworkTask.setUnblock();
                chatNetworkTask.execute().get();
            } catch (Exception exc) { } // Handle Exceptions
        } else {
            // Handle network not available
            Snackbar.make(findViewById(android.R.id.content),
                    R.string.network_not_available,
                    Snackbar.LENGTH_SHORT).show();
        }

        startBackgroundTask();
    }

    public void blockOpponent(String OpponentId) {

        AlertDialog.Builder dialog = new AlertDialog.Builder(ChatActivity.this);
        dialog.setMessage(OpponentId + "님을 차단하시겠습니까?")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        blockOpponentProcess(OpponentId);
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void blockOpponentProcess(String OpponentId) {

        asyncManager.clear();

        if (isNetworkAvailable()) {
            try {
                ChatTask chatNetworkTask
                        = new ChatTask(this, mUser.getId(), OpponentId);
                chatNetworkTask.setBlock();
                chatNetworkTask.execute().get();
            } catch (Exception exc) { } // Handle Exceptions
        } else {
            // Handle network not available
            Snackbar.make(findViewById(android.R.id.content),
                    R.string.network_not_available,
                    Snackbar.LENGTH_SHORT).show();
        }

        startBackgroundTask();
    }

    public void startChat(String OpponentId) {

        Intent chatRoomActivityIntent
                = new Intent(this, ChatRoomActivity.class);
        chatRoomActivityIntent.putExtra("OPPONENT_ID", OpponentId);
        startActivity(chatRoomActivityIntent);
    }

    private void handleBlockedOpponent() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(ChatActivity.this);
        dialog.setMessage(mOpponentId + "님은\n차단된 채팅 상대입니다.")
                .setPositiveButton("차단 해제", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        unblockOpponent();
                        startChat(mOpponentId);
                    }
                })
                .setNegativeButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void loadChatOpponent() {

        if (isNetworkAvailable()) {
            try {
                LoadChatOpponentTask loadChatOpponentTask
                        = new LoadChatOpponentTask(
                                false, false, asyncManager,
                                mUser.getId(), mOpponentId,
                                mOpponentList, mAdapter);
                boolean isBlock
                        = loadChatOpponentTask.onPost(loadChatOpponentTask.execute().get());

                if (!mOpponentId.equals("")) {  // 채팅 상대를 지정한 경우
                    if (isBlock)
                        handleBlockedOpponent();
                    else
                        startChat(mOpponentId); // 지정된 상대방과의 채팅 시작
                }
            } catch (Exception exc) { } // Handle Exceptions
        } else {
            // Handle network not available
            Snackbar.make(findViewById(android.R.id.content),
                    R.string.network_not_available,
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    private void startBackgroundTask() {

        LoadChatOpponentTask loadChatOpponentTask
                = new LoadChatOpponentTask(
                true, true, asyncManager,
                mUser.getId(), mOpponentId,
                mOpponentList, mAdapter);

        loadChatOpponentTask.execute();
    }
}