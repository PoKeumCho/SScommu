package com.sscommu.pokeumcho;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 *  Scroll event for recyclerview inside scrollview android
 *  [참고] https://stackoverflow.com/questions/32697498/scroll-event-for-recyclerview-inside-scrollview-android/33728160
 */
public class EndlessScrollView extends ScrollView {

    private EndlessScrollListener endlessScrollListener = null;

    public EndlessScrollView(Context context) {
        super(context);
    }

    public EndlessScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public EndlessScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setScrollViewListener(EndlessScrollListener endlessScrollListener) {
        this.endlessScrollListener = endlessScrollListener;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldL, int oldT) {
        super.onScrollChanged(l, t, oldL, oldT);
        if (endlessScrollListener != null) {
            endlessScrollListener.onScrollChanged(this, l, t, oldL, oldT);
        }
    }
}
