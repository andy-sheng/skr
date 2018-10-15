package com.wali.live.watchsdk.bigturntable.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.mi.live.data.repository.GiftRepository;
import com.mi.live.data.repository.model.turntable.PrizeItemModel;
import com.wali.live.dao.Gift;
import com.wali.live.proto.BigTurnTableProto;
import com.wali.live.watchsdk.R;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhujianning on 18-7-10.
 * 大转盘ui绘制
 */

public class BigTurnTableContentView extends View {
    private static final String TAG = "BigTurnTableContentView";
    private static final int PX_DES_V_OFFSET = DisplayUtils.dip2px(33f);
    private static final int PX_INTRO_V_OFFSET = DisplayUtils.dip2px(55f);
    private static final int MAX_WIDTH = 770;

    //paint
    private Paint mTextIntroPaint;
    private Paint mTextDesPaint;
    private Paint mBgPaint;

    //data
    private List<PrizeItemModel> mDatas;
    private float mAngle;
    private int mCenter;
    private int mRadius;
    private int mWidth;
    private int mTextDesColorId = GlobalData.app().getResources().getColor(R.color.color_e9d7fe);
    private float mTextDesTextSize = DisplayUtils.dip2px(9.33f);
    private int mTextIntroColorId = GlobalData.app().getResources().getColor(R.color.color_white);
    private float mTextIntroTextSize = DisplayUtils.dip2px(10.67f);
    private float mCurAngle;
    private HashMap<String, Bitmap> mBmpMap;

    public BigTurnTableContentView(Context context) {
        this(context, null);
    }

    public BigTurnTableContentView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BigTurnTableContentView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setBackgroundColor(Color.TRANSPARENT);

        mTextIntroPaint = new Paint();
        mTextIntroPaint.setStyle(Paint.Style.STROKE);
        mTextIntroPaint.setAntiAlias(true);
        mTextIntroPaint.setDither(true);
        mTextIntroPaint.setColor(mTextIntroColorId);
        mTextIntroPaint.setTypeface(Typeface.DEFAULT_BOLD);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mTextIntroPaint.setLetterSpacing(0.5f);
        }
        mTextIntroPaint.setTextSize(mTextIntroTextSize);

        mTextDesPaint = new Paint();
        mTextDesPaint.setStyle(Paint.Style.STROKE);
        mTextDesPaint.setAntiAlias(true);
        mTextDesPaint.setDither(true);
        mTextDesPaint.setColor(mTextDesColorId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mTextDesPaint.setLetterSpacing(0.5f);
        }
        mTextDesPaint.setTextSize(mTextDesTextSize);

        //最底部画笔
        mBgPaint = new Paint();
        mBgPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mBgPaint.setStrokeWidth(4f);
        mBgPaint.setAntiAlias(true);
        mBgPaint.setDither(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = MAX_WIDTH;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int width;
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(desiredWidth, widthSize);
        } else {
            width = desiredWidth;
        }
        mWidth = width;
        mCenter = mWidth / 2;
        mRadius = mWidth / 2;
        setMeasuredDimension(width, width);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mDatas == null || mDatas.size() == 0) {
            MyLog.d(TAG, "onDraw mDatas is null");
            return;
        }
        float startAngle = - 90;

        for (int i = 0; i < mDatas.size(); i++) {
            PrizeItemModel itemModel = mDatas.get(i);
            if (itemModel.isCustom()) {
                drawText(startAngle, GlobalData.app().getResources().getString(R.string.hosts_send_gift_to_you), mRadius, PX_DES_V_OFFSET, mTextDesPaint, canvas);
                drawText(startAngle, itemModel.getCustomDes(), mRadius, mRadius / 2, mTextIntroPaint, canvas);
            } else {
                if (itemModel.getGiftType() == BigTurnTableProto.GiftType.VIRTUAL_DIAMOND_VALUE) {
                    drawText(startAngle, String.format(GlobalData.app().getResources().getString(R.string.x_num_tips), String.valueOf(itemModel.getTimes())), mRadius, PX_INTRO_V_OFFSET, mTextIntroPaint, canvas);
                    drawText(startAngle, GlobalData.app().getResources().getString(R.string.silver_diamond), mRadius, PX_DES_V_OFFSET, mTextDesPaint, canvas);
                } else {
                    Gift giftById = GiftRepository.findGiftById(itemModel.getGiftId());
                    if (giftById != null) {
                        drawText(startAngle, giftById.getName(), mRadius, PX_INTRO_V_OFFSET, mTextIntroPaint, canvas);
                    } else {
                        drawText(startAngle, GlobalData.app().getResources().getString(R.string.gift), mRadius, PX_INTRO_V_OFFSET, mTextIntroPaint, canvas);
                    }

                    BigTurnTableProto.ToWhom toWhom = itemModel.getToWhom();
                    if(toWhom == BigTurnTableProto.ToWhom.ANCHOR) {
                        drawText(startAngle, GlobalData.app().getResources().getString(R.string.send_gift_to_beauty), mRadius, PX_DES_V_OFFSET, mTextDesPaint, canvas);
                    } else {
                        drawText(startAngle, GlobalData.app().getResources().getString(R.string.put_into_package), mRadius, PX_DES_V_OFFSET, mTextDesPaint, canvas);
                    }
                }
            }

            if(!itemModel.isCustom()
                    && !TextUtils.isEmpty(itemModel.getGiftPic())
                    && mBmpMap != null) {
                String ext = "";
                if(itemModel.getGiftType() == BigTurnTableProto.GiftType.VIRTUAL_DIAMOND_VALUE) {
                    ext = String.valueOf(itemModel.getTimes());
                } else {
                    Gift gift = GiftRepository.findGiftById(itemModel.getGiftId());
                    if(gift != null) {
                        ext = gift.getName();
                    }
                }
                String key = itemModel.getGiftPic() + ext;
                if(mBmpMap.get(key) != null) {
                    int imgWidth = mRadius / 4;
                    int w = ( int ) (Math.abs(Math.cos(Math.toRadians(Math.abs(180 - mAngle * i)))) *
                            imgWidth + imgWidth * Math.abs(Math.sin(Math.toRadians(Math.abs(180 - mAngle * i)))));
                    int h = ( int ) (Math.abs(Math.sin(Math.toRadians(Math.abs(180 - mAngle * i)))) *
                            imgWidth + imgWidth * Math.abs(Math.cos(Math.toRadians(Math.abs(180 - mAngle * i)))));
                    float angle = (float) ((mAngle / 2 + startAngle) * (Math.PI / 180));
                    float x = ( float ) (mCenter + (3 * mRadius / 7) * Math.cos(angle));
                    float y = ( float ) (mCenter + (3 * mRadius / 7) * Math.sin(angle));
                    RectF rect1 = new RectF(x - w / 2, y - h / 2, x + w / 2, y + h / 2);
                    canvas.drawBitmap(mBmpMap.get(key), null, rect1, null);
                }
            }
            startAngle = startAngle + mAngle;
        }
    }

    private void drawText(float startAngle, String text, int radius, float vOffset, Paint textPaint, Canvas canvas) {
        Path circlePath = new Path();
        RectF rect = new RectF(mCenter - radius, mCenter - radius, mCenter
                + radius, mCenter + radius);
        circlePath.addArc(rect, startAngle, mAngle);
        float textWidth = textPaint.measureText(text);//圆弧的水平偏移
        float hOffset = (float) (Math.sin(mAngle / 2 / 180 * Math.PI) * radius) - textWidth / 2;//圆弧的垂直偏移
        canvas.drawTextOnPath(text, circlePath, hOffset, vOffset, textPaint);
    }

    public void setDatas(List<PrizeItemModel> datas, HashMap<String, Bitmap> bmpMap) {
        this.mDatas = datas;
        mAngle = (float) (360.0 / mDatas.size());
        this.mBmpMap = bmpMap;

        show();
    }

    private void show() {
        MyLog.d(TAG, "show");
        if (mDatas == null || mDatas.isEmpty()) {
            MyLog.e(TAG, "mDatas is null");
            return;
        }
        invalidate();
    }

    public float getCurAngle() {
        return mCurAngle;
    }

    public void setCurAngle(float curAngle) {
        this.mCurAngle = curAngle;
    }

    public float getAngle() {
        return mAngle;
    }

    public void setAngle(float angle) {
        this.mAngle = angle;
    }

    public void destory() {
        clearMap();
    }

    public void clearMap() {
        if(mBmpMap != null
                && !mBmpMap.isEmpty()) {
            Iterator<Map.Entry<String, Bitmap>> iterator = mBmpMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Bitmap> next = iterator.next();
                Bitmap value = next.getValue();
                value.recycle();
                value = null;
            }
            System.gc();
        }
    }

}
