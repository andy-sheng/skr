package com.base.image.fresco.view;

import android.content.Context;
import android.util.AttributeSet;

import com.base.image.fresco.BaseImageView;
import com.base.log.MyLog;

/**
 * Created by lan on 16/5/23.
 */
public class TransferImageView extends BaseImageView {
    private int mInitTranslationY = 0;

    public TransferImageView(Context context) {
        super(context);
    }

    public TransferImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TransferImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void provideInitTranslationY(int initTranslationY) {
        mInitTranslationY = initTranslationY;
        setTranslationY(0);
    }

    @Override
    public void setTranslationY(float translationY) {
        float actualY = translationY + mInitTranslationY;
        MyLog.d(TAG, "actualY=" + actualY);
        super.setTranslationY(actualY);
    }
}
