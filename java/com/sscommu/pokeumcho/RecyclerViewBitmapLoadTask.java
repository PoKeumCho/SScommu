package com.sscommu.pokeumcho;

import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.net.URL;
import java.util.ArrayList;

public class RecyclerViewBitmapLoadTask
        extends AsyncTask<Void, Void, Void> {

    private String mUrlPrefix;
    private ImageView mImageView;
    private Object mObject;

    private ArrayList<AsyncTask> mAsyncTaskList;

    public RecyclerViewBitmapLoadTask(
            String urlPrefix, ImageView imageView, Object object,
            ArrayList<AsyncTask> asyncTaskList) {

        mUrlPrefix = urlPrefix;
        mImageView = imageView;
        mObject = object;

        mAsyncTaskList = asyncTaskList;
        mAsyncTaskList.add(this);
    }

    @Override
    protected Void doInBackground(Void... voids) {

        URL url;

        try {
            /* Trade */
            if (mObject instanceof Trade) {
                url = new URL(mUrlPrefix + ((Trade) mObject).getImgPath()[0]);
                ((Trade) mObject).setRepresentativeBitmap(
                        BitmapFactory.decodeStream(url.openConnection().getInputStream()));
            }
            /* ChatData */
            else if (mObject instanceof ChatData) {
                url = new URL(mUrlPrefix + ((ChatData) mObject).getContent());
                ((ChatData) mObject).setBitmap(
                        BitmapFactory.decodeStream(url.openConnection().getInputStream()));
            }
        } catch (Exception exc) {
            // Handle exceptions
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void unused) {
        super.onPostExecute(unused);

        /* Trade */
        if (mObject instanceof Trade) {
            if (((Trade) mObject).getRepresentativeBitmap() != null) {
                mImageView.setImageBitmap(((Trade) mObject).getRepresentativeBitmap());
                mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else {
                mImageView.setImageResource(R.drawable.no_image_found);
                mImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            }
        }
        /* ChatData */
        else if (mObject instanceof ChatData) {
            if (((ChatData) mObject).getBitmap() != null) {
                mImageView.setImageBitmap(((ChatData) mObject).getBitmap());
                mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else {
                mImageView.setImageResource(R.drawable.no_image_found);
                mImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            }
        }

        // To force a view to draw, call invalidate().
        mImageView.invalidate();

        mAsyncTaskList.remove(this);
    }
}
