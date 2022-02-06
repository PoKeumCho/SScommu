package com.sscommu.pokeumcho;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class BitmapPagerAdapter extends PagerAdapter {

    Context context;

    Bitmap[] bitmaps;
    LayoutInflater inflater;

    // set additional options
    boolean isCenterCrop;
    boolean isClickable;
    AppCompatActivity activity;

    public BitmapPagerAdapter(Context context, Bitmap[] bitmaps) {

        this.context = context;
        this.bitmaps = bitmaps;

        // default value
        isCenterCrop = false;
        isClickable = false;
        activity = null;
    }

    public BitmapPagerAdapter(
            Context context, Bitmap[] bitmaps, boolean isCenterCrop) {

        this.context = context;
        this.bitmaps = bitmaps;
        this.isCenterCrop = isCenterCrop;

        // default value
        isClickable = false;
        activity = null;
    }

    public void setClickable(AppCompatActivity activity) {

        this.isClickable = true;
        this.activity = activity;
    }

    @Override
    public int getCount() {

        return bitmaps.length;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        ImageView image;

        inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        View itemView = inflater.inflate(
                R.layout.pager_item, container, false);

        if (isClickable) {
            if (activity instanceof ViewTradeActivity) {
                itemView.setClickable(true);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((ViewTradeActivity)activity).viewPagerOnClick(position);
                    }
                });
            }
        }

        // get a reference to imageView in paper_item layout
        image = itemView.findViewById(R.id.imageView);

        if (isCenterCrop)
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);

        // Set an image to the ImageView
        image.setImageBitmap(bitmaps[position]);

        // Add pager_item layout as the current page to the ViewPager
        ((ViewPager) container).addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container,
                            int position,
                            @NonNull Object object) {

        // Remove pager_item layout from ViewPager
        container.removeView((RelativeLayout) object);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {

        return view == object;
    }
}
