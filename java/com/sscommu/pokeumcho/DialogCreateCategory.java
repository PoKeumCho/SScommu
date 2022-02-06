package com.sscommu.pokeumcho;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.HashMap;
import java.util.Map;

public class DialogCreateCategory extends DialogFragment {

    private String mUserId;

    private String mResultMsg;
    private GeneralCategory mCategory;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_create_category, null);

        final EditText editTextName = dialogView.findViewById(R.id.editTextName);
        final EditText editTextInfo = dialogView.findViewById(R.id.editTextInfo);
        final Switch switchExpel = dialogView.findViewById(R.id.switchExpel);
        final Button btnOk = dialogView.findViewById(R.id.btnOk);

        final TextView nameErrorTxt = dialogView.findViewById(R.id.nameErrorTxt);
        nameErrorTxt.setVisibility(View.GONE);

        builder.setView(dialogView);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editTextName.getText().toString().trim();
                String info = editTextInfo.getText().toString().trim();
                boolean expel = switchExpel.isChecked();

                /* 기존 데이터 초기화 */
                mResultMsg = "";
                nameErrorTxt.setVisibility(View.GONE);

                if (name.equals("") || info.equals("")) {
                    Toast.makeText(getContext(),
                            R.string.dialog_create_category_edittext_error,
                            Toast.LENGTH_SHORT).show();
                    editTextName.setText(name);
                    editTextInfo.setText(info);
                } else {
                    if (isNetworkAvailable()) {
                        try {
                            CreateGeneralCategoryNetworkTask createGeneralCategoryNetworkTask
                                    = new CreateGeneralCategoryNetworkTask(name, info, expel);
                            String jsonString = createGeneralCategoryNetworkTask.execute().get();
                            if (onPostCreateGeneralCategoryNetworkTask(jsonString)) {
                                /** 새 게시판 만들기 성공 시 */
                                startGeneralActivity(mCategory);
                                dismiss();
                            } else {
                                /** 이미 사용 중인 게시판 이름이 존재하는 경우 */
                                if (mResultMsg.equals("NAME ERROR")) {
                                    nameErrorTxt.setVisibility(View.VISIBLE);
                                    editTextName.setText("");
                                } else {
                                    Toast.makeText(getContext(),
                                            R.string.default_error_message,
                                            Toast.LENGTH_SHORT).show();
                                    dismiss();
                                }
                            }
                        } catch (Exception exc) {
                            dismiss();
                        }
                    } else {    // Network not available
                        Toast.makeText(getContext(),
                                R.string.network_not_available, Toast.LENGTH_SHORT).show();
                        dismiss();
                    }
                }
            }
        });

        return builder.create();
    }

    /** Receive userId from the Activity */
    public void sendUserId(String userId) {
        mUserId = userId;
    }


    public class CreateGeneralCategoryNetworkTask
            extends AsyncTask<Void, Void, String> {

        private String mName;
        private String mInfo;
        private String mExpel;

        public CreateGeneralCategoryNetworkTask(String name, String info, boolean expel) {
            mName = name;
            mInfo = info;
            mExpel = expel ? "Y" : "N";
        }

        @Override
        protected String doInBackground(Void... voids) {
            String result;

            Map<String, String> queryMap = new HashMap();
            queryMap.put("id", mUserId);
            queryMap.put("name", mName);
            queryMap.put("info", mInfo);
            queryMap.put("expel", mExpel);

            SimpleHttpJSON simpleHttpJSON
                    = new SimpleHttpJSON("createGeneralCategory", queryMap);
            try {
                result = simpleHttpJSON.sendPost();
            } catch (Exception exc) {
                result = "";
            }
            return result;
        }
    }

    private boolean onPostCreateGeneralCategoryNetworkTask(String jsonString) {

        if (jsonString.equals("")) { return false; }
        try {
            // Parsing String with JSONTokener to JSONObject
            JSONTokener tokener = new JSONTokener(jsonString);
            JSONObject response = new JSONObject(tokener);

            if (response.getBoolean("result")) {
                /** 새 게시판 만들기 성공 시 */
                JSONObject category = response.getJSONObject("category");
                mCategory = new GeneralCategory(category);
                return true;
            } else {
                mResultMsg = response.getString("msg").trim();
                return false;
            }
        } catch (JSONException exc) {
            return false;
        }
    }

    private void startGeneralActivity(GeneralCategory category) {

        Intent generalActivityIntent
                = new Intent(getActivity(), GeneralActivity.class);
        generalActivityIntent.putExtra("CATEGORY_ID", category.getId());
        generalActivityIntent.putExtra("CATEGORY_USERID", category.getUserId());
        generalActivityIntent.putExtra("CATEGORY_NAME", category.getName());
        generalActivityIntent.putExtra("CATEGORY_INFO", category.getInfo());
        startActivity(generalActivityIntent);
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
