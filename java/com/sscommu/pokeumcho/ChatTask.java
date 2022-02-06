package com.sscommu.pokeumcho;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Base64;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatTask
        extends AsyncTask<Void,Void,String> {

    private AppCompatActivity mActivity;
    private String mUserId;
    private String mOpponentId;

    private String mOption;

    private int mNo;            // Update read status
    private String mText;       // Send Text
    private UploadImage mFile;  // Send File

    private ArrayList<ChatData> mChatDataList;
    private ArrayList<ChatData> mDisplayChatList;


    public ChatTask(AppCompatActivity activity, String userId, String opponentId) {

        mActivity = activity;
        mUserId = userId;
        mOpponentId = opponentId;
    }

    public void setUpdateReadStatus(int no) {
        reset();
        mOption = "updateReadStatus";
        mNo = no;
    }

    public void setSendText(String text,
                            ArrayList<ChatData> chatDataList,
                            ArrayList<ChatData> displayChatList) {
        reset();
        mOption = "sendText";
        mText = text;
        mChatDataList = chatDataList;
        mDisplayChatList = displayChatList;
    }

    public void setSendFile(UploadImage file,
                            ArrayList<ChatData> chatDataList,
                            ArrayList<ChatData> displayChatList) {
        reset();
        mOption = "sendFile";
        mFile = file;
        mChatDataList = chatDataList;
        mDisplayChatList = displayChatList;
    }

    public void setBlock() {
        reset();
        mOption = "block";
    }

    public void setUnblock() {
        reset();
        mOption = "unblock";
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (mOption.equals("sendFile"))
            ((ChatRoomActivity) mActivity).startProgressDialog();
    }

    @Override
    protected String doInBackground(Void... voids) {

        String result;

        Map<String, String> queryMap = new HashMap();
        queryMap.put("id", mUserId);
        queryMap.put("opponent_id", mOpponentId);
        queryMap.put("option", mOption);
        queryMap.put("no", String.valueOf(mNo));
        queryMap.put("text", mText);
        if (mFile != null) {
            queryMap.put("image_name", mFile.getName());
            queryMap.put("image_path", convertImageFile());
            queryMap.put("image_width", String.valueOf(mFile.getBitmap().getWidth()));
            queryMap.put("image_height", String.valueOf(mFile.getBitmap().getHeight()));
        }

        SimpleHttpJSON simpleHttpJSON
                = new SimpleHttpJSON("chat", queryMap);
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

        if (mOption.equals("sendFile")) {
            ((ChatRoomActivity) mActivity).onPostSend(onPostSend(s));
            ((ChatRoomActivity) mActivity).stopProgressDialog();
        }
    }

    public int onPostSend(String jsonString) {

        try {
            // Parsing String with JSONTokener to JSONObject
            JSONTokener tokener = new JSONTokener(jsonString);
            JSONObject response = new JSONObject(tokener);

            if (response.getBoolean("result")) {

                JSONArray jArray = response.getJSONArray("chat");
                ChatData chat = new ChatData(jArray.getJSONObject(0), mUserId);
                mChatDataList.add(chat);
                mDisplayChatList.add(chat);
                return mDisplayChatList.indexOf(chat);
            }
        } catch (JSONException exc) {
            // Handle exceptions
        }

        return -1;
    }

    private String convertImageFile() {
        String convertImage;

        ByteArrayOutputStream byteArrayOutputStreamObject ;
        byteArrayOutputStreamObject = new ByteArrayOutputStream();

        // 업로드하는 이미지는 모두 JPEG 로 변환되어 저장된다.
        mFile.compress(Bitmap.CompressFormat.JPEG,
                100, byteArrayOutputStreamObject);
        mFile.changeFileExtension("jpeg");

        byte[] byteArrayVar = byteArrayOutputStreamObject.toByteArray();
        convertImage = Base64.encodeToString(byteArrayVar, Base64.DEFAULT);

        return convertImage;
    }

    private void reset() {
        mOption = "";
        mNo = 0;
        mText = "";
        mFile = null;
        mChatDataList = null;
        mDisplayChatList = null;
    }
}
