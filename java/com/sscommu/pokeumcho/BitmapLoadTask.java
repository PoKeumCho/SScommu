package com.sscommu.pokeumcho;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.net.URL;

/**
 *  Get image from web
 *  [참고] https://velog.io/@dlrmwl15/안드로이드-웹에서-이미지-가져오기
 */
public class BitmapLoadTask extends AsyncTask<Void, Void, Boolean> {

    private int mCount;
    private String mUrlPrefix;
    private Bitmap[] mBitmaps;
    private String[] mPaths;

    public BitmapLoadTask(int count, String urlPrefix,
                          Bitmap[] bitmaps, String[] paths) {

        mCount = count;
        mUrlPrefix = urlPrefix;
        mBitmaps = bitmaps;
        mPaths = paths;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {

        URL url;
        Bitmap bitmap;

        try {
            for (int i = 0; i < mCount; i++) {
                url = new URL(mUrlPrefix + mPaths[i]);
                bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                mBitmaps[i] = bitmap;
            }
        } catch (Exception exc) {
            // Handle exceptions
            return false;
        }
        return true;
    }
}