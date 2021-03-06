package com.sscommu.pokeumcho;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EditTradeActivity extends AppCompatActivity
        implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final int MAX_IMAGES = 3;

    private static final String IMAGE_NAME = "image_name_"; // _0λΆν° μμ
    private static final String IMAGE_PATH = "image_path_"; // _0λΆν° μμ
    private static final String IMAGE_WIDTH = "image_width_"; // _0λΆν° μμ
    private static final String IMAGE_HEIGHT = "image_height_"; // _0λΆν° μμ

    private User mUser;
    private SharedPreferences mPrefs;
    private SharedPreferences.Editor mEditor;

    private int mSelectedCategory;
    private Spinner categorySpinner;
    private String[] mCategory;

    private EditText editTextTitle;
    private EditText editTextPrice;
    private EditText editTextInfo;

    private TextView campusTxt;
    private CheckBox checkBox_S;
    private CheckBox checkBox_U;

    private ArrayList<UploadImage> mUploadImages;

    private Button btnUploadImage;
    private ConstraintLayout[] mImageLayoutArray;
    private ImageView[] mImageViewArray;
    private ImageButton[] mBtnDeleteArray;
    private ImageButton[] mBtnRotateArray;

    ActivityResultLauncher<Intent> myActivityResultLauncher;
    ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_trade);
        setTitle("μ€κ³ κ±°λ κΈμ°κΈ°");

        mPrefs = getSharedPreferences("SScommu", MODE_PRIVATE);
        mEditor = mPrefs.edit();

        mUser = new User();
        mUser.setId(mPrefs.getString("USER_ID", ""));
        mUser.setNickname(mPrefs.getString("USER_NICKNAME", ""));
        mUser.setAccountImgId(mPrefs.getInt("USER_ACCOUNT_IMG_ID", 0));

        // To create a back button in the title bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        /** μ΄λ―Έμ§ μλ‘λ */
        mUploadImages = new ArrayList<UploadImage>();
        myActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        customActivityResult(result);
                    }
                });

        loadUI();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.btnUploadImage:
                if (mUploadImages.size() < MAX_IMAGES) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    myActivityResultLauncher.launch(intent);
                } else {
                    Snackbar.make(view,
                            R.string.edit_trade_activity_upload_image_info,
                            Snackbar.LENGTH_SHORT)
                            .show();
                }
                break;

            /** μλ‘λν μ΄λ―Έμ§ μ­μ  */
            case R.id.btnDelete_1:
                deleteUploadImage(0);
                break;
            case R.id.btnDelete_2:
                deleteUploadImage(1);
                break;
            case R.id.btnDelete_3:
                deleteUploadImage(2);
                break;

            /** μλ‘λν μ΄λ―Έμ§ νμ  */
            case R.id.btnRotate_1:
                rotateUploadImage(0);
                break;
            case R.id.btnRotate_2:
                rotateUploadImage(1);
                break;
            case R.id.btnRotate_3:
                rotateUploadImage(2);
                break;
        }
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

    private void loadUI() {

        categorySpinner = findViewById(R.id.categorySpinner);
        editTextTitle = findViewById(R.id.editTextTitle);
        editTextPrice = findViewById(R.id.editTextPrice);
        editTextInfo = findViewById(R.id.editTextInfo);

        if (isNetworkAvailable()) {
            setCategoryArray();
            setSpinner();
        } else {
            Toast.makeText(this,
                    R.string.network_not_available,
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        campusTxt = findViewById(R.id.campusTxt);
        checkBox_S = findViewById(R.id.checkBox_S);
        checkBox_U = findViewById(R.id.checkBox_U);
        checkBox_S.setOnCheckedChangeListener(this);
        checkBox_U.setOnCheckedChangeListener(this);

        initiateUploadArray();

        for (ConstraintLayout layout: mImageLayoutArray) {
            layout.setVisibility(View.GONE);
        }
        for (ImageButton button: mBtnDeleteArray) {
            button.setOnClickListener(this);
        }
        for (ImageButton button: mBtnRotateArray) {
            button.setOnClickListener(this);
        }

        btnUploadImage = findViewById(R.id.btnUploadImage);
        btnUploadImage.setOnClickListener(this);

        // Add a Floating Action Button
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { submit(); }
        });
    }

    private void initiateUploadArray() {

        mImageLayoutArray = new ConstraintLayout[] {
                findViewById(R.id.imageLayout_1),
                findViewById(R.id.imageLayout_2),
                findViewById(R.id.imageLayout_3)
        };
        mImageViewArray = new ImageView[] {
                findViewById(R.id.imageView_1),
                findViewById(R.id.imageView_2),
                findViewById(R.id.imageView_3)
        };
        mBtnDeleteArray = new ImageButton[] {
                findViewById(R.id.btnDelete_1),
                findViewById(R.id.btnDelete_2),
                findViewById(R.id.btnDelete_3)
        };
        mBtnRotateArray = new ImageButton[] {
                findViewById(R.id.btnRotate_1),
                findViewById(R.id.btnRotate_2),
                findViewById(R.id.btnRotate_3)
        };
    }

    private void setSpinner() {

        ArrayAdapter<String> adapter;
        adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_dropdown_item, mCategory);
        categorySpinner.setAdapter(adapter);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // κΈμ ν¬κΈ° 16spλ‘ μ€μ νλ€.
                ((TextView) parent.getChildAt(0)).setTextSize(16);

                // [--μΉ΄νκ³ λ¦¬--] κΈμ μ μ§μ  (default status)
                if (position == 0)
                    ((TextView) parent.getChildAt(0))
                            .setTextColor(getResources().getColor(R.color.hint_grey));

                mSelectedCategory = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    // μ€κ³ κ±°λ μΉ΄νκ³ λ¦¬ λͺ©λ‘μ κ°μ Έμ¨λ€.
    private void setCategoryArray() {

        TradeCategory tradeCategoryHelper = new TradeCategory();
        mCategory = tradeCategoryHelper.getCategoryArray();

        if (mCategory == null)
            mCategory = new String[] { "[-- μΉ΄νκ³ λ¦¬ --]" };
    }

    /** μμ±ν μ€κ³ κ±°λ κΈ μ μ‘ */
    private void submit() {

        boolean valid = true;
        StringBuilder dialogSb = new StringBuilder();

        String title = editTextTitle.getText().toString().trim();
        int price;
        try {
            price = Integer.parseInt(editTextPrice.getText().toString().trim());
        } catch (Exception exc) {   // μ μ λ³ν μ€λ₯ λ°μ μ
            price = -1;
        }
        String info = editTextInfo.getText().toString().trim();

        /* μ λͺ© */
        if (title.equals("")) {
            valid = false;
            dialogSb.append("- μ λͺ©μ μλ ₯ν΄μ£ΌμΈμ.\n");
            editTextTitle.setText("");
        }
        /* μΉ΄νκ³ λ¦¬ */
        if (mSelectedCategory == 0) {
            valid = false;
            dialogSb.append("- μΉ΄νκ³ λ¦¬λ₯Ό μ νν΄μ£ΌμΈμ.\n");
        }
        /* κ°κ²© */
        if (price < 0 || price > 1000000) {
            valid = false;
            dialogSb.append("- κ±°λ κΈμ‘μ 0μ μ΄μ, 100λ§μ μ΄νλ‘ μ€μ ν΄μ£ΌμΈμ.\n");
            editTextPrice.setText("");
        }
        /* μΊ νΌμ€ */
        if (!checkBox_S.isChecked() && !checkBox_U.isChecked()) {
            valid = false;
            dialogSb.append("- μΊ νΌμ€λ₯Ό μ νν΄μ£ΌμΈμ.\n");
        }
        /* μ€λͺ */
        if (info.equals("")) {
            valid = false;
            dialogSb.append("- κ²μκΈ λ΄μ©μ μμ±ν΄μ£ΌμΈμ.\n");
            editTextInfo.setText("");
        }
        /* μ΄λ―Έμ§ */
        if (mUploadImages.size() == 0) {
            valid = false;
            dialogSb.append("- μ΄λ―Έμ§λ₯Ό μ²¨λΆν΄μ£ΌμΈμ.\n");
        }

        if (valid) {
            UploadToServerFunction();
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(EditTradeActivity.this);
            dialog.setMessage(dialogSb.toString())
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        if (isChecked)
            buttonView.setTextColor(Color.BLACK);
        else
            buttonView.setTextColor(getResources().getColor(R.color.hint_grey));

        if (checkBox_S.isChecked() || checkBox_U.isChecked())
            campusTxt.setTextColor(Color.BLACK);
        else
            campusTxt.setTextColor(getResources().getColor(R.color.hint_grey));
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void UploadToServerFunction() {

        String trade_category = String.valueOf(mSelectedCategory);
        String trade_title = editTextTitle.getText().toString().trim();
        String trade_price = editTextPrice.getText().toString().trim();
        String trade_info = editTextInfo.getText().toString().trim();
        String trade_campus;
        if (checkBox_S.isChecked() && checkBox_U.isChecked())
            trade_campus = "B";
        else if (checkBox_S.isChecked())
            trade_campus = "S";
        else
            trade_campus = "U";


        final ArrayList<String> ConvertImages = new ArrayList<>();

        ByteArrayOutputStream byteArrayOutputStreamObject ;
        byteArrayOutputStreamObject = new ByteArrayOutputStream();

        for (UploadImage uploadImage: mUploadImages) {

            // μλ‘λνλ μ΄λ―Έμ§λ λͺ¨λ JPEG λ‘ λ³νλμ΄ μ μ₯λλ€.
            uploadImage.compress(Bitmap.CompressFormat.JPEG,
                    100, byteArrayOutputStreamObject);
            uploadImage.changeFileExtension("jpeg");

            byte[] byteArrayVar = byteArrayOutputStreamObject.toByteArray();
            ConvertImages.add(Base64.encodeToString(byteArrayVar, Base64.DEFAULT));

            byteArrayOutputStreamObject.reset();
        }

        class AsyncTaskUploadClass extends AsyncTask<Void,Void,String> {

            @Override
            protected void onPreExecute() {

                super.onPreExecute();

                mProgressDialog = ProgressDialog.show(
                        EditTradeActivity.this,
                        "μλ‘λ μ€μλλ€.",
                        "μ μλ§ κΈ°λ€λ € μ£ΌμΈμ.",
                        false,false);
            }

            @Override
            protected void onPostExecute(String jsonString) {

                super.onPostExecute(jsonString);

                // Dismiss the progress dialog after done uploading.
                mProgressDialog.dismiss();

                try {
                    // Parsing String with JSONTokener to JSONObject
                    JSONTokener tokener = new JSONTokener(jsonString);
                    JSONObject response = new JSONObject(tokener);

                    if (response.getBoolean("result")) {

                        /* μλ‘λ μ±κ³΅ μ */
                        mEditor.putBoolean("TRADE_ADD_SUCCESS", true);

                        mEditor.putInt("TRADE_ADD_ID", response.getInt("id"));
                        mEditor.putString("TRADE_ADD_CATEGORY", mCategory[mSelectedCategory]);
                        mEditor.putInt("TRADE_ADD_CATEGORY_ID", mSelectedCategory);
                        mEditor.putString("TRADE_ADD_TITLE", trade_title);
                        mEditor.putInt("TRADE_ADD_PRICE", Integer.parseInt(trade_price));
                        mEditor.putString("TRADE_ADD_INFO", trade_info);
                        mEditor.putString("TRADE_ADD_FIRST_IMG_PATH",
                                response.getString("first_img_path"));
                        mEditor.putString("TRADE_ADD_CAMPUS", trade_campus);
                        mEditor.putString("TRADE_ADD_DATE", response.getString("date"));

                        mEditor.commit();
                    } else {
                        Toast.makeText(EditTradeActivity.this,
                                "FAIL", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException exc) {
                    // Handle exceptions
                    Toast.makeText(EditTradeActivity.this,
                            "FAIL", Toast.LENGTH_SHORT).show();
                }

                finish();
            }

            @Override
            protected String doInBackground(Void... params) {

                String result;

                Map<String, String> queryMap = new HashMap();
                queryMap.put("id", mUser.getId());
                queryMap.put("trade_category", trade_category);
                queryMap.put("trade_title", trade_title);
                queryMap.put("trade_price", trade_price);
                queryMap.put("trade_info", trade_info);
                queryMap.put("trade_campus", trade_campus);

                int index = 0;
                for (UploadImage uploadImage: mUploadImages) {
                    queryMap.put(IMAGE_NAME + String.valueOf(index), uploadImage.getName());
                    queryMap.put(IMAGE_PATH + String.valueOf(index), ConvertImages.get(index));
                    queryMap.put(IMAGE_WIDTH + String.valueOf(index),
                            String.valueOf(uploadImage.getBitmap().getWidth()));
                    queryMap.put(IMAGE_HEIGHT + String.valueOf(index),
                            String.valueOf(uploadImage.getBitmap().getHeight()));

                    index++;
                }

                SimpleHttpJSON simpleHttpJSON
                        = new SimpleHttpJSON("editTrade", queryMap);
                try {
                    result = simpleHttpJSON.sendPost();
                } catch (Exception exc) {
                    result = "";
                }
                return result;
            }
        }

        if (isNetworkAvailable()) {
            AsyncTaskUploadClass asyncTaskUploadObj = new AsyncTaskUploadClass();
            asyncTaskUploadObj.execute();
        } else {
            Toast.makeText(EditTradeActivity.this,
                    R.string.network_not_available, Toast.LENGTH_SHORT).show();
        }
    }

    /** μλ‘λν μ΄λ―Έμ§ μ­μ  */
    private void deleteUploadImage(int position) {

        mUploadImages.remove(position);

        for (int i = 0; i < MAX_IMAGES; i++) {

            if (i < mUploadImages.size()) {
                mImageLayoutArray[i].setVisibility(View.VISIBLE);
                mImageViewArray[i].setImageBitmap(mUploadImages.get(i).getBitmap());
            } else {
                mImageLayoutArray[i].setVisibility(View.GONE);
            }
        }
    }

    /** μλ‘λν μ΄λ―Έμ§ νμ  */
    private void rotateUploadImage(int position) {

        mUploadImages.get(position).rotateBitmap();

        mImageViewArray[position].setImageBitmap(
                mUploadImages.get(position).getBitmap());
    }

    /** μ΄λ―Έμ§ νμ  Dialog λ₯Ό λμ°κ³  μ΄λ₯Ό Activity νλ©΄μ λ°μνλ€. */
    private void uploadImageProcess(UploadImage uploadImage) {

        DialogRotateImage dialog =
                new DialogRotateImage(uploadImage,
                        mUploadImages,
                        mImageLayoutArray[mUploadImages.size()],
                        mImageViewArray[mUploadImages.size()]);

        dialog.show(getSupportFragmentManager(), "123");
    }

    /** μ΄λ―Έμ§ μλ‘λ */
    private void customActivityResult(ActivityResult result) {

        UploadImage uploadImage;

        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();

            if (data != null && data.getData() != null) {
                Uri uri = data.getData();

                String mimeType = getContentResolver().getType(uri);

                Cursor returnCursor =
                        getContentResolver().query(uri,
                                null, null, null, null);
                /*
                 * Get the column indexes of the data in the Cursor,
                 * move to the first row in the Cursor, get the data,
                 */
                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                returnCursor.moveToFirst();
                String name =  returnCursor.getString(nameIndex);
                Long size = returnCursor.getLong(sizeIndex);

                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    uploadImage = new UploadImage(bitmap, mimeType, name, size);
                    uploadImageProcess(uploadImage);
                } catch (IOException e) {
                    // Handle exceptions
                }
            }
        }
    }
}