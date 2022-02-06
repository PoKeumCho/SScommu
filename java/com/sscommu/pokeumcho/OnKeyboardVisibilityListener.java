package com.sscommu.pokeumcho;

/**
 *  Capture the "keyboard show/hide" event in Android
 *  [참고] https://stackoverflow.com/questions/4312319/how-to-capture-the-virtual-keyboard-show-hide-event-in-android
 */

public interface OnKeyboardVisibilityListener {
    void onVisibilityChanged(boolean visible);
}
