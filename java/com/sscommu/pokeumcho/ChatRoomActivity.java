package com.sscommu.pokeumcho;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;

public class ChatRoomActivity extends AppCompatActivity
        implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    private final String URL_PREFIX = "https://sscommu.com/file/images/chat/";

    private final int MIN_DISPLAY = 20;     // 최소 출력 개수
    private final int MAX_IMAGE_LOAD = 10;  // 미리 가져올 이미지 최대 개수

    private User mUser;
    private SharedPreferences mPrefs;

    private String mOpponentId;

    private SwipeRefreshLayout refreshLayout;
    private ImageButton btnAddImage;
    private ImageButton btnWrite;
    private ImageView imageView;
    private EditText editTextTextMultiLine;

    UploadImage mUploadImage;

    ActivityResultLauncher<Intent> getImageActivityResultLauncher;
    ProgressDialog mProgressDialog;

    private boolean mIsDataSetBefore;

    /**
     *  [ 최초에 불러온 데이터 개수 ]
     *  프로그램 실행 중간에 mChatDataList, mDisplayChatList 에 채팅 데이터를 추가하는 경우
     *  반드시 함께 값을 갱신해야한다는 점에 유의하자. (-> setDisplayChatData() 함수)
     */
    private int mInitialChatDataSize;

    private int mDateCounter;           // 추가된 '날짜' 데이터 개수
    private ArrayList<ChatData> mChatDataList;

    private int firstVisibleItemPosition;
    private int lastVisibleItemPosition;

    private ArrayList<ChatData> mDisplayChatList;
    private RecyclerView recyclerView;
    private ChatDataAdapter mAdapter;

    private AsyncManager asyncManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

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

        // 채팅 상대방 정보
        Intent intent = getIntent();
        mOpponentId = intent.getStringExtra("OPPONENT_ID");
        setTitle(mOpponentId);

        // 이미지 업로드
        mUploadImage = null;
        getImageActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        getImageActivityResult(result);
                    }
                });

        loadUI();

        mIsDataSetBefore = false;
        mDateCounter = 0;
        mChatDataList = new ArrayList<ChatData>();
        mDisplayChatList = new ArrayList<ChatData>();
        asyncManager = new AsyncManager();

        firstVisibleItemPosition = 0;
        lastVisibleItemPosition = 0;

        setRecyclerView();
        loadChatData();
        setDisplayChatData();
        startBackgroundTask();
    }

    @Override
    public void onRefresh() {
        refreshLayout.setRefreshing(false);
        setDisplayChatData();   // 이전 데이터를 불러온다.
    }

    @Override
    protected void onPause() {
        super.onPause();
        asyncManager.clear();

        // 읽음 상태의 채팅을 서버에 반영한다.
        updateReadStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundTask();
    }

    /**
     *  타이틀 바 (상단메뉴) 기능 구현
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.chat_room_top_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            // Back button in the title bar
            case android.R.id.home:
                finish();
                return true;

            case R.id.action_block:
                blockOpponent();
                return true;
        }
        return false;
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.btnAddImage:
                addImageButtonClick();
                break;
            case R.id.btnWrite:
                writeButtonClick();
                break;
        }
    }

    private void loadUI() {

        // To detect recyclerView pull up
        refreshLayout = findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(this);

        btnAddImage = findViewById(R.id.btnAddImage);
        btnWrite = findViewById(R.id.btnWrite);
        imageView = findViewById(R.id.imageView);
        editTextTextMultiLine = findViewById(R.id.editTextTextMultiLine);

        btnAddImage.setOnClickListener(this);
        btnWrite.setOnClickListener(this);

        imageView.setVisibility(View.GONE);
        editTextTextMultiLine.setVisibility(View.VISIBLE);
    }

    private void setRecyclerView() {

        recyclerView = findViewById(R.id.recyclerView);
        mAdapter = new ChatDataAdapter(this,
                mDisplayChatList, asyncManager.getTaskList());

        RecyclerView.LayoutManager layoutManager
                = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // set the adapter
        recyclerView.setAdapter(mAdapter);

        /** Get visible items in RecyclerView */
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                firstVisibleItemPosition
                        = ((LinearLayoutManager) recyclerView.getLayoutManager())
                        .findFirstVisibleItemPosition();
                lastVisibleItemPosition
                        = ((LinearLayoutManager) recyclerView.getLayoutManager())
                        .findLastVisibleItemPosition();

                // 화면에 보여지는 '상대방이 보낸 채팅' 중 읽지 않음으로 된 채팅을 읽음 상태로 변경한다.
                for (int i = firstVisibleItemPosition; i <= lastVisibleItemPosition; i++) {
                    if (isReceivedAndNotRead(i, mDisplayChatList))
                        mDisplayChatList.get(i).setReadStatus(true);
                }

                Log.e("DEBUG", "" + firstVisibleItemPosition + "/" + lastVisibleItemPosition
                        + "[" + mDisplayChatList.size() + "]");
            }
        });
    }

    /** 추가된 '날짜'(DATE) 데이터 관리 */
    public void addDateCounter() { mDateCounter++; }
    public int getDateCounter() { return mDateCounter; }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void loadChatData() {

        mInitialChatDataSize = 0;

        if (isNetworkAvailable()) {
            try {
                LoadChatDataTask loadChatDataTask
                        = new LoadChatDataTask(this,
                        false, false, asyncManager,
                        mUser.getId(), mOpponentId,
                        mChatDataList, mAdapter);

                loadChatDataTask.onPost(loadChatDataTask.execute().get());
                mInitialChatDataSize = mChatDataList.size();
            } catch (Exception exc) { } // Handle Exceptions
        } else {
            // Handle network not available
            Snackbar.make(findViewById(android.R.id.content),
                    R.string.network_not_available,
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    /** 처음 보여질 데이터와 이전 데이터 불러오기를 담당한다. */
    private void setDisplayChatData() {

        final int LAST_POSITION = mInitialChatDataSize - mDisplayChatList.size();

        // 처리할 데이터가 존재하지 않는 경우
        if (LAST_POSITION < 1) return;

        /** 중간에 for 문을 벗어나지 않은 경우에 i 값은 (LAST_POSITION + 1)이 된다. */
        int i;
        for (i = 1; i <= LAST_POSITION; i++) {
            int index = LAST_POSITION - i;
            mDisplayChatList.add(0, mChatDataList.get(index));

            if (mChatDataList.get(index).getViewType() == ChatData.ViewType.DATE
                    && i >= MIN_DISPLAY) {
                // 상대방이 보낸 채팅 중 읽지 않은 경우 (추가)
                if (isReceivedAndNotRead(index, mChatDataList))
                    continue;
                else
                    break;
            }
        }

        /* [이미지 로드 속도 차이가 없는 걸로 보여서 주석 처리]
        // 처음 데이터를 표시하는 경우 이미지를 미리 불러온다.
        if (!mIsDataSetBefore)
            preloadImages();
        mIsDataSetBefore = true;
        */

        mAdapter.notifyDataSetChanged();
        scrollTo(getScrollPosition(Math.min(i, LAST_POSITION)));
    }

    private void preloadImages() {

        try {
            ArrayList<String> pathList = new ArrayList<String>();
            for (ChatData chat : mDisplayChatList) {
                if (chat.getContentType() == ChatData.ContentType.FILE)
                    pathList.add(chat.getContent());
            }
            final int size = Math.min(pathList.size(), MAX_IMAGE_LOAD);
            String[] paths = new String[size];
            Bitmap[] bitmaps = new Bitmap[size];
            for (int i = 0; i < size; i++)
                paths[i] = pathList.get(i);
            BitmapLoadTask bitmapLoadTask
                    = new BitmapLoadTask(size, URL_PREFIX, bitmaps, paths);
            if (bitmapLoadTask.execute().get()) {
                for (int i = 0; i < size; i++) {
                    if (mDisplayChatList.get(i).getContentType() == ChatData.ContentType.FILE)
                        mDisplayChatList.get(i).setBitmap(bitmaps[i]);
                }
            }
        } catch (Exception exc) {
            // Handle Exceptions
        }
    }

    private int getScrollPosition(int diff) {

        // 상대방이 보낸 채팅 중 읽지 않은 경우
        for (int i = 0; i < diff; i++) {
            if (isReceivedAndNotRead(i, mDisplayChatList))
                return i;
        }
        //return (diff - 1);
        // 조금 더 자연스러운 효과를 주기 위해 변경
        if (diff < mDisplayChatList.size()) {
            if (diff == (mDisplayChatList.size() - 1)) { return diff; }
            else { return (diff + 1); }
        } else
            return (diff - 1);
    }

    private void scrollTo(int position) { recyclerView.scrollToPosition(position); }

    /** 상대방이 보낸 채팅인 경우 true 반환 */
    private boolean isReceived(int index, ArrayList<ChatData> chatList) {
        return (chatList.get(index).getViewType() == ChatData.ViewType.OTHER_TEXT
                || chatList.get(index).getViewType() == ChatData.ViewType.OTHER_FILE);
    }

    /** 상대방이 보낸 채팅 중 읽지 않은 경우 true 반환 */
    private boolean isReceivedAndNotRead(int index, ArrayList<ChatData> chatList) {
        return (isReceived(index, chatList) && !chatList.get(index).getReadStatus());
    }

    private void startBackgroundTask() {

        LoadChatDataTask loadChatDataTask
                = new LoadChatDataTask(this,
                true, true, asyncManager,
                mUser.getId(), mOpponentId,
                mChatDataList, mAdapter);

        loadChatDataTask.execute();
    }

    /** 추가된 데이터 처리 */
    public void chatDataListener(int diff) {

        final boolean scrollPosAtEndOfScreen
                = isScrollPosAtEndOfScreen();

        final int start = mChatDataList.size() - diff;

        /* 이전에 읽어온 데이터가 존재하지 않는 경우 */
        if (mInitialChatDataSize == 0) {
            mInitialChatDataSize = mChatDataList.size();
            setDisplayChatData();
        }
        /* 이전에 읽어온 데이터가 존재하는 경우 */
        else {
            int lastIndexOfNotReadChatDataList = -1;
            for (int i = start; i < mChatDataList.size(); i++) {
                // 상대방이 보낸 '안 읽음 상태' 데이터가 있는지 확인한다.
                if (isReceivedAndNotRead(i, mChatDataList))
                    lastIndexOfNotReadChatDataList = i;
                // 추가된 데이터를 화면에 추가한다.
                mDisplayChatList.add(mChatDataList.get(i));
                mInitialChatDataSize++;
            }
            mAdapter.notifyDataSetChanged();

            /** 상대방이 보낸 '안 읽음 상태' 데이터가 존재하며 */
            if (lastIndexOfNotReadChatDataList != -1) {
                int lastIndexOfNotReadDisplayChatList
                        = mDisplayChatList.indexOf(
                                mChatDataList.get(lastIndexOfNotReadChatDataList));
                /** 사용자의 현재 화면에 추가되지 않은 경우 */
                if (scrollPosAtEndOfScreen)
                    scrollTo(lastIndexOfNotReadDisplayChatList);
                else if (!isChatVisibleInUserScreen(lastIndexOfNotReadDisplayChatList)) {
                    Snackbar.make(findViewById(android.R.id.content),
                            R.string.chat_alarm, Snackbar.LENGTH_SHORT)
                            .setAction("이동", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    scrollTo(lastIndexOfNotReadDisplayChatList);
                                }
                            })
                            .show();
                }
            }
        }

        Log.e("DEBUG", "" + diff + "개의 데이터 추가");
    }

    private boolean isChatVisibleInUserScreen(int index) {
        return (firstVisibleItemPosition <= index && index <= lastVisibleItemPosition);
    }

    private boolean isScrollPosAtEndOfScreen() {
        return (lastVisibleItemPosition == mDisplayChatList.size() - 1);
    }

    /** 읽음 상태의 채팅을 서버에 반영한다. */
    private void updateReadStatus() {

        ChatTask chatNetworkTask
                = new ChatTask(this, mUser.getId(), mOpponentId);

        // 상대방이 보낸 채팅 리스트
        ArrayList<ChatData> receivedChatList = new ArrayList<ChatData>();
        // 상대방이 보낸 채팅 리스트 중 '첫 번째' 읽지 않은 상태의 채팅 데이터
        ChatData notReadChat = null;
        for (int i = 0; i < mDisplayChatList.size(); i++) {
            if (isReceived(i, mDisplayChatList)) {
                ChatData receivedChat = mDisplayChatList.get(i);
                receivedChatList.add(receivedChat);
                if (notReadChat == null && !receivedChat.getReadStatus())
                    notReadChat = receivedChat;
            }
        }
        // 상대방이 보낸 채팅이 존재하는 경우에만 실행한다.
        if (receivedChatList.size() > 0) {
            // 모두 다 읽음 상태인 경우
            if (notReadChat == null)
                chatNetworkTask.setUpdateReadStatus(
                        receivedChatList.get(receivedChatList.size()-1).getId() + 1);
                // 읽지 않은 채팅이 존재하는 경우
            else
                chatNetworkTask.setUpdateReadStatus(notReadChat.getId());

            chatNetworkTask.execute();
        }
    }

    private void writeButtonClick() {

        asyncManager.clear();

        if (mUploadImage == null) {
            // Send Text
            String text = editTextTextMultiLine.getText().toString().trim();
            if (text.equals("")) {
                Snackbar.make(findViewById(android.R.id.content),
                        R.string.write_chat, Snackbar.LENGTH_SHORT).show();
            } else { sendTextTask(text); }
            editTextTextMultiLine.setText("");
        } else {
            // Send File
            sendFileTask();
            removeUploadedImage();
        }

        startBackgroundTask();
    }

    private void sendTextTask(String text) {

        if (isNetworkAvailable()) {
            try {
                ChatTask chatNetworkTask
                        = new ChatTask(this, mUser.getId(), mOpponentId);
                chatNetworkTask.setSendText(text, mChatDataList, mDisplayChatList);
                onPostSend(chatNetworkTask.onPostSend(chatNetworkTask.execute().get()));
            } catch (Exception exc) {
                // Handle exceptions
            }
        } else {
            // Handle network not available
            Snackbar.make(findViewById(android.R.id.content),
                    R.string.network_not_available,
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    private void sendFileTask() {

        if (isNetworkAvailable()) {
            ChatTask chatNetworkTask
                    = new ChatTask(this, mUser.getId(), mOpponentId);
            chatNetworkTask.setSendFile(mUploadImage, mChatDataList, mDisplayChatList);
            chatNetworkTask.execute();
        } else {
            // Handle network not available
            Snackbar.make(findViewById(android.R.id.content),
                    R.string.network_not_available,
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    /** 채팅 전송 후 후처리 */
    public void onPostSend(int position) {
        if (position != -1) {
            mInitialChatDataSize++;
            scrollTo(position);
        }
    }

    public void startProgressDialog() {
        mProgressDialog = ProgressDialog.show(
                ChatRoomActivity.this,
                "업로드 중입니다.",
                "잠시만 기다려 주세요.",
                false,false);
    }
    public void stopProgressDialog() { mProgressDialog.dismiss(); }

    public void clickImageView(String path) {

        Intent imageViewIntent = new Intent(this, ImageViewActivity.class);

        imageViewIntent.putExtra("IMAGE_COUNT", 1);
        imageViewIntent.putExtra("IMAGE_POSITION", 0);
        imageViewIntent.putExtra("URL_PREFIX", URL_PREFIX);
        imageViewIntent.putExtra("IMAGE_PATH_0", path);

        startActivity(imageViewIntent);
    }

    /** 이미지 업로드 */

    private void addImageButtonClick() {

        if (mUploadImage == null) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            getImageActivityResultLauncher.launch(intent);

            btnAddImage.setImageResource(R.drawable.delete_icon);
        } else { removeUploadedImage(); }
    }

    private void removeUploadedImage() {

        mUploadImage = null;
        imageView.setImageResource(android.R.color.transparent);

        imageView.setVisibility(View.GONE);
        editTextTextMultiLine.setVisibility(View.VISIBLE);

        btnAddImage.setImageResource(R.drawable.add_image_icon);
    }

    private void getImageActivityResult(ActivityResult result) {

        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                String mimeType = getContentResolver().getType(uri);
                Cursor returnCursor =
                        getContentResolver().query(uri,
                                null, null, null, null);
                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                returnCursor.moveToFirst();
                String name =  returnCursor.getString(nameIndex);
                Long size = returnCursor.getLong(sizeIndex);
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    mUploadImage = new UploadImage(bitmap, mimeType, name, size);
                    uploadImageProcess(mUploadImage);
                } catch (IOException e) { }   // Handle exceptions
            }
        }

        if (mUploadImage == null)
            btnAddImage.setImageResource(R.drawable.add_image_icon);
    }

    // 이미지 회전 Dialog 를 띄우고 이를 화면에 반영한다.
    private void uploadImageProcess(UploadImage uploadImage) {

        imageView.setVisibility(View.VISIBLE);
        editTextTextMultiLine.setVisibility(View.GONE);

        DialogRotateImage dialog = new DialogRotateImage(uploadImage, imageView);
        dialog.show(getSupportFragmentManager(), "123");
    }

    /** 차단 기능 구현 */
    private void blockOpponent() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(ChatRoomActivity.this);
        dialog.setMessage(mOpponentId + "님을 차단하시겠습니까?")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        blockOpponentProcess();
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void blockOpponentProcess() {

        asyncManager.clear();
        boolean result = false;     // 차단 성공 여부

        if (isNetworkAvailable()) {
            try {
                ChatTask chatNetworkTask
                        = new ChatTask(this, mUser.getId(), mOpponentId);
                chatNetworkTask.setBlock();
                chatNetworkTask.execute().get();
                result = true;
            } catch (Exception exc) { } // Handle Exceptions
        } else {
            // Handle network not available
            Snackbar.make(findViewById(android.R.id.content),
                    R.string.network_not_available,
                    Snackbar.LENGTH_SHORT).show();
        }

        if (result) {   // 차단 성공 시
            updateReadStatus();
            finish();
        } else          // 차단 실패 시
            startBackgroundTask();
    }
}