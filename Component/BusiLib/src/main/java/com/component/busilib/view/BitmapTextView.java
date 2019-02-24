package com.component.busilib.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.common.utils.U;
import com.component.busilib.R;

import java.util.ArrayList;
import java.util.List;

public class BitmapTextView extends View {

    List<Bitmap> mBitmapList = new ArrayList<>();
    int diff = U.getDisplayUtils().dip2px(5);  //两张图片的偏移量重合部分
    int mWidth = 0;// view的宽度
    int mHeight = 0;// view的高度

    public BitmapTextView(Context context) {
        super(context);
    }

    public BitmapTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BitmapTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mBitmapList == null || mBitmapList.size() <= 0) {
            return;
        }

        drawText(canvas, mBitmapList);
    }

    private void drawText(Canvas canvas, List<Bitmap> bitmaps) {
        float left = 0;
        for (Bitmap bitmap : bitmaps) {
            canvas.drawBitmap(bitmap, left, 0, new Paint());
            left = left + bitmap.getWidth() - diff;
        }
    }

    private Bitmap getBitmap(char aChar) {
        switch (aChar) {
            case '0':
                return BitmapFactory.decodeResource(getResources(), R.drawable.pk_zhanji_0);
            case '1':
                return BitmapFactory.decodeResource(getResources(), R.drawable.pk_zhanji_1);
            case '2':
                return BitmapFactory.decodeResource(getResources(), R.drawable.pk_zhanji_2);
            case '3':
                return BitmapFactory.decodeResource(getResources(), R.drawable.pk_zhanji_3);
            case '4':
                return BitmapFactory.decodeResource(getResources(), R.drawable.pk_zhanji_4);
            case '5':
                return BitmapFactory.decodeResource(getResources(), R.drawable.pk_zhanji_5);
            case '6':
                return BitmapFactory.decodeResource(getResources(), R.drawable.pk_zhanji_6);
            case '7':
                return BitmapFactory.decodeResource(getResources(), R.drawable.pk_zhanji_7);
            case '8':
                return BitmapFactory.decodeResource(getResources(), R.drawable.pk_zhanji_8);
            case '9':
                return BitmapFactory.decodeResource(getResources(), R.drawable.pk_zhanji_9);
            case '.':
                return BitmapFactory.decodeResource(getResources(), R.drawable.pk_zhanji_dian);
            default:
                return null;
        }
    }

    public void setText(String text) {
        mWidth = 0;
        mHeight = 0;
        if (!TextUtils.isEmpty(text)) {
            mBitmapList.clear();
            char[] chars = text.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                Bitmap bitmap = getBitmap(chars[i]);
                mWidth = mWidth + bitmap.getWidth();
                if (mHeight < bitmap.getHeight()) {
                    mHeight = bitmap.getHeight();
                }
                mBitmapList.add(bitmap);
            }
            mWidth = mWidth - diff * (chars.length - 1);
            invalidate();
        }
    }


}
