package com.module.rankingmode.prepare.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public abstract class Sprite extends RelativeLayout {
    public Sprite(Context context) {
        this(context, null);
    }

    public Sprite(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Sprite(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    abstract void init();

    abstract void onChangeRotation(float rotaion);

    abstract void onChangeWindow(int witdh, int height);

    abstract int[] getIconLocation();

    abstract int[] getSize();
}
