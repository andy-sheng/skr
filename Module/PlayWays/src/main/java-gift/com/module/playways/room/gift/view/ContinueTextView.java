package com.module.playways.room.gift.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.common.log.MyLog;
import com.component.busilib.view.BitmapTextView;
import com.glidebitmappool.BitmapFactoryAdapter;
import com.module.playways.R;

import java.util.List;

public class ContinueTextView extends BitmapTextView {
    Bitmap x;

    public ContinueTextView(Context context) {
        super(context);
        init(context, null);
    }

    public ContinueTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ContinueTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
        initXBitmap();
    }

    protected void drawText(Canvas canvas, List<Bitmap> bitmaps) {
        initXBitmap();

        if (mBitmapList.size() > 0 && mBitmapList.get(0) != x) {
            mBitmapList.add(0, x);
        }
        MyLog.d(TAG, "drawText" + " canvas=" + canvas + " bitmaps=" + bitmaps);
        super.drawText(canvas, bitmaps);
    }

    private void initXBitmap() {
        if (x == null) {
            x = BitmapFactoryAdapter.decodeResource(getResources(), R.drawable.daojishi_x);
        }
    }

    @Override
    public void requestLayout() {
        initXBitmap();
        mWidth += x.getWidth() * scale;
        super.requestLayout();
    }
}
