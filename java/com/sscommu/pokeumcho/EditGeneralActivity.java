package com.sscommu.pokeumcho;

/**
 *  Android Upload Image To Server
 *  [참고] https://androidjson.com/android-upload-image-server-using-php-mysql/
 */

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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

public class EditGeneralActivity extends AppCompatActivity
        implements View.OnClickListener {

    private static final int MAX_IMAGES = 5;

    private static final String IMAGE_NAME = "image_name_"; // _0부터 시작
    private static final String IMAGE_PATH = "image_path_"; // _0부터 시작
    private static final String IMAGE_WIDTH = "image_width_"; // _0부터 시작
    private static final String IMAGE_HEIGHT = "image_height_"; // _0부터 시작

    private User mUser;
    private SharedPreferences mPrefs;

    private GeneralCategory mGeneralCategory;

    private ArrayList<UploadImage> mUploadImages;

    private Button btnUploadImage;
    private ConstraintLayout[] mImageLayoutArray;
    private ImageView[] mImageViewArray;
    private ImageButton[] mBtnDeleteArray;
    private ImageButton[] mBtnRotateArray;

    ActivityResultLauncher<Intent> myActivityResultLauncher;
    ProgressDialog mProgressDialog;

    private EditText editText;
    private String mText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_general);
        setTitle("글 쓰기");

        mPrefs = getSharedPreferences("SScommu", MODE_PRIVATE);

        mUser = new User();
        mUser.setId(mPrefs.getString("USER_ID", ""));
        mUser.setNickname(mPrefs.getString("USER_NICKNAME", ""));
        mUser.setAccountImgId(mPrefs.getInt("USER_ACCOUNT_IMG_ID", 0));

        Intent intent = getIntent();
        int categoryId = intent.getIntExtra("CATEGORY_ID", 0);
        if (categoryId == 0) {
            finish();
        }

        mGeneralCategory = new GeneralCategory();
        mGeneralCategory.setId(categoryId);
        mGeneralCategory.setUserId(intent.getStringExtra("CATEGORY_USERID"));
        mGeneralCategory.setName(intent.getStringExtra("CATEGORY_NAME"));
        mGeneralCategory.setInfo(intent.getStringExtra("CATEGORY_INFO"));

        // To create a back button in the title bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        editText = findViewById(R.id.editText);

        // Add a Floating Action Button
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String text = editText.getText().toString().trim();

                if (text.equals("")) {
                    Snackbar.make(view,
                            "내용을 입력하세요.",
                            Snackbar.LENGTH_SHORT).show();
                    editText.setText(text);
                } else {
                    mText = text;
                    UploadToServerFunction();
                }
            }
        });

        /** 이미지 업로드 */
        mUploadImages = new ArrayList<UploadImage>();

        myActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        customActivityResult(result);
                    }
                });
        btnUploadImage = findViewById(R.id.btnUploadImage);
        btnUploadImage.setOnClickListener(this);

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
                            R.string.edit_general_activity_upload_image_info,
                            Snackbar.LENGTH_SHORT)
                            .show();
                }
                break;

            /** 업로드한 이미지 삭제 */
            case R.id.btnDelete_1:
                deleteUploadImage(0);
                break;
            case R.id.btnDelete_2:
                deleteUploadImage(1);
                break;
            case R.id.btnDelete_3:
                deleteUploadImage(2);
                break;
            case R.id.btnDelete_4:
                deleteUploadImage(3);
                break;
            case R.id.btnDelete_5:
                deleteUploadImage(4);
                break;

            /** 업로드한 이미지 회전 */
            case R.id.btnRotate_1:
                rotateUploadImage(0);
                break;
            case R.id.btnRotate_2:
                rotateUploadImage(1);
                break;
            case R.id.btnRotate_3:
                rotateUploadImage(2);
                break;
            case R.id.btnRotate_4:
                rotateUploadImage(3);
                break;
            case R.id.btnRotate_5:
                rotateUploadImage(4);
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

    /** 업로드한 이미지 삭제 */
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

    /** 업로드한 이미지 회전 */
    private void rotateUploadImage(int position) {

        mUploadImages.get(position).rotateBitmap();

        mImageViewArray[position].setImageBitmap(
                mUploadImages.get(position).getBitmap());
    }

    /** 이미지 회전 Dialog 를 띄우고 이를 Activity 화면에 반영한다. */
    private void uploadImageProcess(UploadImage uploadImage) {

        DialogRotateImage dialog =
                new DialogRotateImage(uploadImage,
                        mUploadImages,
                        mImageLayoutArray[mUploadImages.size()],
                         mImageViewArray[mUploadImages.size()]);

        dialog.show(getSupportFragmentManager(), "123");
    }

    /** 이미지 업로드 */
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

    public void UploadToServerFunction() {

        final ArrayList<String> ConvertImages = new ArrayList<>();

        ByteArrayOutputStream byteArrayOutputStreamObject ;
        byteArrayOutputStreamObject = new ByteArrayOutputStream();

        for (UploadImage uploadImage: mUploadImages) {

            // 업로드하는 이미지는 모두 JPEG 로 변환되어 저장된다.
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
                        EditGeneralActivity.this,
                        "업로드 중입니다.",
                        "잠시만 기다려 주세요.",
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

                    if (!response.getBoolean("result")) {
                        Toast.makeText(EditGeneralActivity.this,
                                "FAIL", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException exc) {
                    // Handle exceptions
                    Toast.makeText(EditGeneralActivity.this,
                            "FAIL", Toast.LENGTH_SHORT).show();
                }

                finish();
            }

            @Override
            protected String doInBackground(Void... params) {

                String result;

                Map<String, String> queryMap = new HashMap();
                queryMap.put("id", mUser.getId());
                queryMap.put("categoryid", String.valueOf(mGeneralCategory.getId()));
                queryMap.put("text", mText);

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

                SimpleHttpJSON simpleHttpJSON = new SimpleHttpJSON("editGeneral", queryMap);
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
            Toast.makeText(EditGeneralActivity.this,
                    R.string.network_not_available, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    private void initiateUploadArray() {

        mImageLayoutArray = new ConstraintLayout[] {
                findViewById(R.id.imageLayout_1),
                findViewById(R.id.imageLayout_2),
                findViewById(R.id.imageLayout_3),
                findViewById(R.id.imageLayout_4),
                findViewById(R.id.imageLayout_5)
        };
        mImageViewArray = new ImageView[] {
                findViewById(R.id.imageView_1),
                findViewById(R.id.imageView_2),
                findViewById(R.id.imageView_3),
                findViewById(R.id.imageView_4),
                findViewById(R.id.imageView_5)
        };
        mBtnDeleteArray = new ImageButton[] {
                findViewById(R.id.btnDelete_1),
                findViewById(R.id.btnDelete_2),
                findViewById(R.id.btnDelete_3),
                findViewById(R.id.btnDelete_4),
                findViewById(R.id.btnDelete_5),
        };
        mBtnRotateArray = new ImageButton[] {
                findViewById(R.id.btnRotate_1),
                findViewById(R.id.btnRotate_2),
                findViewById(R.id.btnRotate_3),
                findViewById(R.id.btnRotate_4),
                findViewById(R.id.btnRotate_5),
        };
    }
}