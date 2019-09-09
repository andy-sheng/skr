package com.component.busilib.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.common.log.MyLog;
import com.common.utils.U;
import com.component.busilib.R;
import com.glidebitmappool.BitmapFactoryAdapter;
import com.glidebitmappool.BitmapPoolAdapter;

import java.util.ArrayList;
import java.util.List;

public class BitmapTextView extends View {

    public final String TAG = "BitmapTextView";

    protected List<Bitmap> mBitmapList = new ArrayList<>();
    int diff = U.getDisplayUtils().dip2px(2);  //两张图片的偏移量重合部分
    protected int mWidth = 0;// view的宽度
    protected int mHeight = 0;// view的高度

    protected float scale;   //图片放缩比例
    int textColor; //图片文字颜色
    boolean hasShadow;  //是否有阴影

    public BitmapTextView(Context context) {
        super(context);
        init(context, null);
    }

    public BitmapTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public BitmapTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    protected void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BitmapTextView);
        scale = typedArray.getFloat(R.styleable.BitmapTextView_scale, 1.0f);
        textColor = typedArray.getColor(R.styleable.BitmapTextView_text_color, 0);
        hasShadow = typedArray.getBoolean(R.styleable.BitmapTextView_has_shadow, false);
        typedArray.recycle();
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

    protected void drawText(Canvas canvas, List<Bitmap> bitmaps) {
        float left = 0;
        for (Bitmap bitmap : bitmaps) {
            if (bitmap.isRecycled()) {
                continue;
            }
            canvas.drawBitmap(bitmap, left, 0, new com.common.view.ExPaint());
            left = left + bitmap.getWidth() - diff * scale;
        }
    }

    protected Bitmap getBitmap(char aChar) {
        if (hasShadow) {
            switch (aChar) {
                case '0':
                    return BitmapFactoryAdapter.decodeResource(getResources(), R.drawable.daojishi_0);
                case '1':
                    return BitmapFactoryAdapter.decodeResource(getResources(), R.drawable.daojishi_1);
                case '2':
                    return BitmapFactoryAdapter.decodeResource(getResources(), R.drawable.daojishi_2);
                case '3':
                    return BitmapFactoryAdapter.decodeResource(getResources(), R.drawable.daojishi_3);
                case '4':
                    return BitmapFactoryAdapter.decodeResource(getResources(), R.drawable.daojishi_4);
                case '5':
                    return BitmapFactoryAdapter.decodeResource(getResources(), R.drawable.daojishi_5);
                case '6':
                    return BitmapFactoryAdapter.decodeResource(getResources(), R.drawable.daojishi_6);
                case '7':
                    return BitmapFactoryAdapter.decodeResource(getResources(), R.drawable.daojishi_7);
                case '8':
                    return BitmapFactoryAdapter.decodeResource(getResources(), R.drawable.daojishi_8);
                case '9':
                    return BitmapFactoryAdapter.decodeResource(getResources(), R.drawable.daojishi_9);
                case '.':
                    return BitmapFactoryAdapter.decodeResource(getResources(), R.drawable.daojishi_dian);
                default:
                    return null;
            }
        } else {
            switch (aChar) {
                case '0':
                    return BitmapFactoryAdapter.decodeResource(getResources(), R.drawable.pk_zhanji_0);
                case '1':return BitmapFactoryAdapter.decodeResource(getResources(), R.drawable.pk_zhanji_1);

                               case '2':
                    return BitmapFactoryAdapter.decodeResource(getResources(), R.drawable.pk_zhanji_2);
                case '3':
                    return BitmapFactoryAdapter.decodeResource(getResources(), R.drawable.pk_zhanji_3);
                case '4':
                    return BitmapFactoryAdapter.decodeResource(getResources(), R.drawable.pk_zhanji_4);
                case '5':
                    return BitmapFactoryAdapter.decodeResource(getResources(), R.drawable.pk_zhanji_5);
                case '6':
                    return BitmapFactoryAdapter.decodeResource(getResources(), R.drawable.pk_zhanji_6);
                case '7':
                    return BitmapFactoryAdapter.decodeResource(getResources(), R.drawable.pk_zhanji_7);
                case '8':
                    return BitmapFactoryAdapter.decodeResource(getResources(), R.drawable.pk_zhanji_8);
                case '9':
                    return BitmapFactoryAdapter.decodeResource(getResources(), R.drawable.pk_zhanji_9);
                case '.':
                    return BitmapFactoryAdapter.decodeResource(getResources(), R.drawable.pk_zhanji_dian);
                default:
                    return null;
            }
        }
    }

    public void setDrawTextColor(int color){
        textColor = color;
    }
    public void setText(String text) {
        mWidth = 0;
        mHeight = 0;
        if (!TextUtils.isEmpty(text)) {
            mBitmapList.clear();
            char[] chars = text.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                Bitmap bitmap = getBitmap(chars[i]);
                if (bitmap != null) {
                    if (scale != 1) {
                        int height = bitmap.getHeight();
                        int width = bitmap.getWidth();
                        Matrix matrix = new Matrix();
                        matrix.postScale(scale, scale);
                        Bitmap newBM = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
                        if (!bitmap.isRecycled()) {
                            BitmapPoolAdapter.putBitmap(bitmap);
                        }
                        if (newBM != null) {
                            mWidth = mWidth + newBM.getWidth();
                            if (mHeight < newBM.getHeight()) {
                                mHeight = newBM.getHeight();
                            }
                            if (textColor != 0) {
                                mBitmapList.add(U.getBitmapUtils().tintBitmap(newBM, textColor));
                            } else {
                                mBitmapList.add(newBM);
                            }
                        } else {
                            MyLog.w(TAG, "setText" + " text=" + text + "newBM is null");
                            MyLog.w(TAG, "scale = " + scale);
                        }
                    } else {
                        mWidth = mWidth + bitmap.getWidth();
                        if (mHeight < bitmap.getHeight()) {
                            mHeight = bitmap.getHeight();
                        }
                        if (textColor != 0) {
                            mBitmapList.add(U.getBitmapUtils().tintBitmap(bitmap, textColor));
                        } else {
                            mBitmapList.add(bitmap);
                        }

                    }
                } else {
                    MyLog.w(TAG, " setText error text=" + text);
                }
            }
            mWidth = mWidth - (int) (diff * (chars.length - 1) * scale);
            requestLayout();
        }
    }


}
