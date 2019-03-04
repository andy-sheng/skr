package com.module.playways.grab.room.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.module.rank.R;

public class RedPkgCountDownView extends FrameLayout {
    public RedPkgCountDownView(Context context) {
        super(context);
        init();
    }

    public RedPkgCountDownView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RedPkgCountDownView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        inflate(getContext(), R.layout.red_pkg_count_down_view_layout,this);
    }
}
