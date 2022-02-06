package com.sscommu.pokeumcho;

/**
 *  Scroll event for recyclerview inside scrollview android
 *  [참고] https://stackoverflow.com/questions/32697498/scroll-event-for-recyclerview-inside-scrollview-android/33728160
 */
public interface EndlessScrollListener {

    void onScrollChanged(EndlessScrollView scrollView, int x, int y, int oldX, int oldY);
}
