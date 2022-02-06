package com.sscommu.pokeumcho;

import android.content.Context;
import android.util.AttributeSet;

import androidx.core.widget.NestedScrollView;

public class EndlessNestedScrollView extends NestedScrollView {

    private EndlessNestedScrollListener endlessNestedScrollListener = null;

    public EndlessNestedScrollView(Context context) {
        super(context);
    }

    public EndlessNestedScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public EndlessNestedScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setScrollViewListener(EndlessNestedScrollListener endlessNestedScrollListener) {
        this.endlessNestedScrollListener = endlessNestedScrollListener;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldL, int oldT) {
        super.onScrollChanged(l, t, oldL, oldT);
        if (endlessNestedScrollListener != null) {
            endlessNestedScrollListener.onScrollChanged(this, l, t, oldL, oldT);
        }
    }
}
