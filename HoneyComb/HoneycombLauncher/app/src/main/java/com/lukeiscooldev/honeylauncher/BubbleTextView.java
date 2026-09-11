package com.lukeiscooldev.honeylauncher;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Styled TextView used for both workspace and all-apps icons, matching the
 * Holo "bubble" press-highlight look from Honeycomb-era launchers.
 */
public class BubbleTextView extends TextView {

    public BubbleTextView(Context context) {
        super(context);
    }

    public BubbleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BubbleTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
