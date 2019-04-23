package com.module.playways.grab.room.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.module.playways.R;

public class RedPkgCountDownView extends RelativeLayout {
    RedCircleProgressView mRedCircleCountDownView;
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
        inflate(getContext(), R.layout.red_pkg_count_down_view_layout, this);
        mRedCircleCountDownView = findViewById(R.id.red_circle_count_down_view);
    }

    public void startCountDown(long duration){
        mRedCircleCountDownView.go(duration);
    }
}
