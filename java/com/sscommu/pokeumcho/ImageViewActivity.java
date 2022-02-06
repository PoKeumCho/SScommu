package com.sscommu.pokeumcho;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.io.OutputStream;

public class ImageViewActivity extends AppCompatActivity {

    private int mCount;
    private int mPosition;
    private String mUrlPrefix;

    private Bitmap[] mImgBitmaps;
    private String[] mImgPaths;

    private ViewPager viewPager;
    private PagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        // To create a back button in the title bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        mCount = intent.getIntExtra("IMAGE_COUNT", 0);
        mPosition = intent.getIntExtra("IMAGE_POSITION", 0);
        mUrlPrefix = intent.getStringExtra("URL_PREFIX");
        if (mCount == 0) {
            finish();
        }

        mImgBitmaps = new Bitmap[mCount];
        mImgPaths = new String[mCount];
        for (int i = 0; i < mCount; i++) {
            mImgPaths[i] = intent.getStringExtra("IMAGE_PATH_" + i);
        }

        setTitle();
        loadImage();
        changePath("jpeg");

        viewPager = findViewById(R.id.viewPager);
        adapter = new BitmapPagerAdapter(ImageViewActivity.this, mImgBitmaps);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(mPosition);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position,
                                       float positionOffset,
                                       int positionOffsetPixels) { }

            @Override
            public void onPageSelected(int position) {
                mPosition = position;
                setTitle();
            }

            @Override
            public void onPageScrollStateChanged(int state) { }
        });
    }

    /**
     *  타이틀 바 (상단메뉴) 기능 구현
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.image_view_top_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            // Back button in the title bar
            case android.R.id.home:
                finish();
                return true;

            case R.id.action_download:
                saveBitmapToGallery();
                return true;
        }

        return false;
    }


    private void setTitle() {
        setTitle(String.valueOf(mPosition + 1) + "/" + String.valueOf(mCount));
    }

    private void loadImage() {

        if (isNetworkAvailable()) {
            try {
                BitmapLoadTask bitmapLoadTask
                        = new BitmapLoadTask(mCount, mUrlPrefix, mImgBitmaps, mImgPaths);
                if (!bitmapLoadTask.execute().get()) {  /* 실패한 경우 */
                    finish();
                }
            } catch (Exception exc) {
                // Handle Exceptions.
                finish();
            }
        } else {
            // Handle network not available.
            Toast.makeText(this,
                    R.string.network_not_available,
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     *  파일명을 gallery 저장명에 알맞게 고친다.
     */
    private void changePath(String extension) {

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < mImgPaths.length; i++) {

            // YYYY-MM-DD/filename.extension --> filename.extension
            String[] parts = mImgPaths[i].split("/");
            for (int j = 1; j < parts.length; j++) {
                sb.append(parts[j]).append('/');
            }
            sb.setLength(sb.length() - 1);

            // Change file extension
            String path = sb.toString();
            sb.setLength(0);
            parts = path.split("\\.");
            for (int j = 0; j < (parts.length - 1); j++) {
                sb.append(parts[j]).append('.');
            }
            sb.append(extension);

            mImgPaths[i] = sb.toString();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    private void saveBitmapToGallery() {

        try {
            Uri uri = saveBitmap(this, mImgBitmaps[mPosition],
                    Bitmap.CompressFormat.JPEG, "image/jpeg",
                    mImgPaths[mPosition]);

            Snackbar.make(findViewById(android.R.id.content),
                    R.string.saved_image_message,
                    Snackbar.LENGTH_SHORT).show();

        } catch (Exception exc) {
            Snackbar.make(findViewById(android.R.id.content),
                    R.string.error_saving_image,
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * How to save an image in Android using MediaStore
     * [참고] https://stackoverflow.com/questions/56904485/how-to-save-an-image-in-android-q-using-mediastore
     */
    @NonNull
    public Uri saveBitmap(@NonNull final Context context,
                          @NonNull final Bitmap bitmap,
                          @NonNull final Bitmap.CompressFormat format,
                          @NonNull final String mimeType,
                          @NonNull final String displayName) throws IOException {

        final ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM);

        final ContentResolver resolver = context.getContentResolver();
        Uri uri = null;

        try {
            final Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            uri = resolver.insert(contentUri, values);

            if (uri == null)
                throw new IOException("Failed to create new MediaStore record.");

            try (final OutputStream stream = resolver.openOutputStream(uri)) {
                if (stream == null)
                    throw new IOException("Failed to open output stream.");

                if (!bitmap.compress(format, 100, stream))
                    throw new IOException("Failed to save bitmap.");
            }

            return uri;
        }
        catch (IOException e) {

            if (uri != null) {
                // Don't leave an orphan entry in the MediaStore
                resolver.delete(uri, null, null);
            }

            throw e;
        }
    }
}