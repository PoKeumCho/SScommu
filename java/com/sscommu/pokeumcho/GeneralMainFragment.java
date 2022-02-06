package com.sscommu.pokeumcho;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GeneralMainFragment extends Fragment
        implements View.OnClickListener {

    private static final String JSON_DEFAULT_USERS = "defaultUsers";
    private static final String JSON_USER_CATEGORY = "bookmarks";

    private String mUserId;
    private ArrayList<GeneralCategory> mUserCategory;

    private LinearLayout myArticleGeneral;          // 내가 쓸 글 clickable layout
    private LinearLayout myArticleGeneralComment;   // 댓글 단 글 clickable layout

    private LinearLayout generalCategoryWrap;

    private Button btnSearch;

    private boolean mIsActiveFragmentMode;

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (hidden) {   /* Do when hidden */
            mIsActiveFragmentMode = false;
        } else {    /* Do when show */
            mIsActiveFragmentMode = true;
            loadGeneralCategory();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mIsActiveFragmentMode) { loadGeneralCategory(); }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        mIsActiveFragmentMode = false;

        // Data which sent from activity
        mUserId = getArguments().getString("USER_ID");

        View view = inflater.inflate(R.layout.content_main_general,
                container, false);

        myArticleGeneral = view.findViewById(R.id.myArticleGeneral);
        myArticleGeneralComment = view.findViewById(R.id.myArticleGeneralComment);
        btnSearch = view.findViewById(R.id.btnSearch);
        generalCategoryWrap = view.findViewById(R.id.generalCategoryWrap);

        myArticleGeneral.setOnClickListener(this);
        myArticleGeneralComment.setOnClickListener(this);
        btnSearch.setOnClickListener(this);

        loadGeneralCategory();
        
        return view;
    }

    /** 즐겨찾기 게시판 */
    private void loadGeneralCategory() {

        if (isNetworkAvailable()) {

            UserCategoryNetworkTask userCategoryNetworkTask = new UserCategoryNetworkTask();
            userCategoryNetworkTask.execute();

        } else {    // Network not available

            // 기존 사용자 즐겨찾기 게시판을 화면에서 제거한다.
            mUserCategory = null;
            generalCategoryWrap.removeAllViews();

            // 네트워크 연결 확인 메시지를 화면에 출력한다.
            TextView networkMsg = new TextView(getContext());
            networkMsg.setText(R.string.network_not_available);
            generalCategoryWrap.addView(networkMsg);
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.myArticleGeneral:
                startMyGeneralArticleActivity(MyArticle.GENERAL);
                break;
            case R.id.myArticleGeneralComment:
                startMyGeneralArticleActivity(MyArticle.COMMENT);
                break;
            case R.id.btnSearch:
                Intent generalCategorySearchIntent
                        = new Intent(getActivity(), GeneralCategorySearchActivity.class);
                startActivity(generalCategorySearchIntent);
                break;
        }
    }

    enum MyArticle { GENERAL, COMMENT };
    private void startMyGeneralArticleActivity(MyArticle option) {

        String title;
        Intent myGeneralArticleIntent
                = new Intent(getActivity(), MyGeneralArticleActivity.class);

        if (option == MyArticle.GENERAL)
            title = getResources().getString(R.string.myarticle_option_general);
        else if (option == MyArticle.COMMENT)
            title = getResources().getString(R.string.myarticle_option_comment);
        else
            title = "";

        myGeneralArticleIntent.putExtra("TITLE", title);

        startActivity(myGeneralArticleIntent);
    }


    /** 즐겨찾기 게시판 */
    public class UserCategoryNetworkTask
            extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            String result;

            Map<String, String> queryMap = new HashMap();
            queryMap.put("id", mUserId);

            SimpleHttpJSON simpleHttpJSON = new SimpleHttpJSON("userCategory", queryMap);
            try {
                result = simpleHttpJSON.sendGet();
            } catch (Exception exc) {
                result = "";
            }
            return result;
        }

        @Override
        protected void onPostExecute(String jsonString) {
            super.onPostExecute(jsonString);

            ArrayList<GeneralCategory> postUserCategory
                    = onPostUserCategoryNetworkTask(jsonString);
            if (postUserCategory != null) {

                // 기존 사용자 즐겨찾기 게시판을 화면에서 제거한다.
                generalCategoryWrap.removeAllViews();

                // 새로운 사용자 즐겨찾기 게시판을 화면에 출력한다.
                for (GeneralCategory category: postUserCategory) {
                    generalCategoryWrap.addView(
                            createGeneralCategoryClickable(getContext(), category));
                }

                mUserCategory = postUserCategory;
            }
        }
    }

    /**
     * @return :
     *      - 게시판 목록이 변경된 경우 변경된 ArrayList 반환
     *      - 게시판 목록이 변경되지 않은 경우 null 반환
     */
    private ArrayList<GeneralCategory> onPostUserCategoryNetworkTask(String jsonString) {

        ArrayList<GeneralCategory> tempUserCategory
                = new ArrayList<GeneralCategory>();

        try {
            // Parsing String with JSONTokener to JSONObject
            JSONTokener tokener = new JSONTokener(jsonString);
            JSONObject response = new JSONObject(tokener);

            // 자유게시판 추가
            tempUserCategory.add(createDefaultCategory(response.getInt(JSON_DEFAULT_USERS)));

            // 사용자의 즐겨찾기 게시판 추가
            JSONArray jArray = response.getJSONArray(JSON_USER_CATEGORY);
            for (int i = 0; i < jArray.length(); i++) {
                tempUserCategory.add(new GeneralCategory(jArray.getJSONObject(i)));
            }

        } catch (JSONException exc) { } // Handle exceptions

        if (isEqualGeneralCategoryArrayList(mUserCategory, tempUserCategory))
            return null;
        else
            return tempUserCategory;
    }

    private boolean isEqualGeneralCategoryArrayList(
            ArrayList<GeneralCategory> prevList, ArrayList<GeneralCategory> postList) {

        if (prevList == null) { return false; }
        else if (prevList.size() != postList.size()) { return false; }
        else {
            for (int i = 0; i < prevList.size(); i++) {
                if (prevList.get(i).getId() != postList.get(i).getId())
                    return false;
            }

            return true;
        }
    }

    /** 자유게시판 생성 */
    private GeneralCategory createDefaultCategory(int users) {
        GeneralCategory defaultCategory = new GeneralCategory();

        defaultCategory.setId(1);
        defaultCategory.setUserId("root");
        defaultCategory.setName("자유게시판");
        defaultCategory.setInfo("");
        defaultCategory.setExpel(false);
        defaultCategory.setUsers(users);

        return defaultCategory;
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
        frameLayout.setLayoutParams(frameLayoutParams);
        frameLayout.setOrientation(LinearLayout.HORIZONTAL);
        frameLayout.setGravity(Gravity.CENTER_VERTICAL);

        /* Image Button */
        LinearLayout.LayoutParams imageButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, // width
                LinearLayout.LayoutParams.WRAP_CONTENT  // height
        );
        imageButtonParams.setMargins(20, 0, 30, 0);
        ImageButton imageButton = new ImageButton(context);
        imageButton.setLayoutParams(imageButtonParams);
        imageButton.setBackgroundColor(Color.WHITE);
        imageButton.setImageResource(R.drawable.star);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleImageButtonOnClick(v, category);
            }
        });
        frameLayout.addView(imageButton);

        /* Clickable TextView */
        LinearLayout.LayoutParams clickableTextParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, // width
                LinearLayout.LayoutParams.WRAP_CONTENT  // height
        );
        TextView clickableText = new TextView(context);
        clickableText.setLayoutParams(clickableTextParams);
        clickableText.setText(category.getName());
        if (category.isExpel()) {
            clickableText.setTextColor(Color.parseColor("#0256B0"));
        } else {
            clickableText.setTextColor(Color.BLACK);
        }
        clickableText.setBackgroundResource(R.drawable.main_clickable_wrapper);
        clickableText.setPadding(30, 10, 30, 10);
        clickableText.setSingleLine(true);
        clickableText.setClickable(true);
        clickableText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleTextViewOnClick(category);
            }
        });
        frameLayout.addView(clickableText);

        return frameLayout;
    }


    /**
     *  동적으로 생성된 즐겨찾기 게시판 UI의 Click 처리
     */

    private void handleImageButtonOnClick(View view, GeneralCategory category) {

        // 자유게시판은 사용자가 즐겨찾기에서 제거할 수 없다.
        if (category.getId() == 1) { return; }

        String message = "[" +  category.getName() + "] 게시판을 즐겨찾기 목록에서 제거하시겠습니까?";

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            if (isNetworkAvailable()) {

                                RemoveBookmarkNetworkTask removeBookmarkNetworkTask
                                        = new RemoveBookmarkNetworkTask(category.getId());
                                String jsonString = removeBookmarkNetworkTask.execute().get();
                                if (onPostRemoveBookmarkNetworkTask(jsonString)) {
                                    /** 즐겨찾기 제거 성공 시 */
                                    ((ViewGroup)view.getParent().getParent())
                                            .removeView((ViewGroup)view.getParent());
                                    mUserCategory.remove(category);
                                } else {
                                    Snackbar.make(getActivity().findViewById(android.R.id.content),
                                            R.string.main_activity_general_remove_category_error,
                                            Snackbar.LENGTH_SHORT).show();
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
    
    private void handleTextViewOnClick(GeneralCategory category) {

        Intent generalActivityIntent
                = new Intent(getActivity(), GeneralActivity.class);
        generalActivityIntent.putExtra("CATEGORY_ID", category.getId());
        generalActivityIntent.putExtra("CATEGORY_USERID", category.getUserId());
        generalActivityIntent.putExtra("CATEGORY_NAME", category.getName());
        generalActivityIntent.putExtra("CATEGORY_INFO", category.getInfo());
        startActivity(generalActivityIntent);
    }


    /**
     *  즐겨찾기 제거
     */
    public class RemoveBookmarkNetworkTask
            extends AsyncTask<Void, Void, String> {

        private int mCategoryId;

        public RemoveBookmarkNetworkTask(int categoryId) {
            mCategoryId = categoryId;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String result;

            Map<String, String> queryMap = new HashMap();
            queryMap.put("id", mUserId);
            queryMap.put("categoryid", String.valueOf(mCategoryId));

            SimpleHttpJSON simpleHttpJSON
                    = new SimpleHttpJSON("removeCategoryBookmark", queryMap);
            try {
                result = simpleHttpJSON.sendPost();
            } catch (Exception exc) {
                result = "";
            }
            return result;
        }
    }
    private boolean onPostRemoveBookmarkNetworkTask(String jsonString) {

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


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
