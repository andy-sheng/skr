package com.zq.lyrics.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.common.log.MyLog;
import com.common.utils.U;
import com.zq.lyrics.model.LyricsLineInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 以秒为基准
 * 音阶view
 */
public class VoiceScaleView2 extends View {
    public final static String TAG = "VoiceScaleView";
    static final int SPEED = U.getDisplayUtils().dip2px(72);// 每秒走72个像素单位
    float mReadLineX = 0.2f;// 红线大约在距离左边 20% 的位置
    float mRedCy = -1;
    int mWidth = -1;// view的宽度
    int mHeight = -1;// view的高度
    long mLocalBeginTs = -1;// 本地开始播放的时间戳，本地基准时间
    long mTranslateTX = 0;// 调用方告知的偏移量

    List<LyricsLineInfo> mLyricsLineInfoList = new ArrayList<>(); // 歌词

    Paint mLeftPaint;
    Paint mRightPaint;
    Paint mRedOutpaint;
    Paint mRedInnerpaint;

    public VoiceScaleView2(Context context) {
        this(context, null);
        init();
    }

    public VoiceScaleView2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init();
    }

    public VoiceScaleView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mLeftPaint = new Paint();
        mLeftPaint.setColor(Color.parseColor("#F5A623"));

        mRightPaint = new Paint();
        mRightPaint.setColor(Color.parseColor("#474A5F"));

        mRedOutpaint = new Paint(); //外圈
        mRedOutpaint.setColor(Color.parseColor("#EF5E85"));
        mRedOutpaint.setAntiAlias(true);

        mRedInnerpaint = new Paint(); //内圈
        mRedInnerpaint.setColor(Color.parseColor("#CA2C60"));
        mRedInnerpaint.setAntiAlias(true);
    }

    /**
     * @param lyricsLineInfoList
     * @param translateTX        调用方告知的偏移量
     */
    public void startWithData(List<LyricsLineInfo> lyricsLineInfoList, int translateTX) {
        MyLog.d(TAG, "startWithData" + " lyricsLineInfoList=" + lyricsLineInfoList);
        this.mLyricsLineInfoList = lyricsLineInfoList;
        this.mLocalBeginTs = -1;
        this.mTranslateTX = translateTX;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mWidth < 0) {
            mWidth = getWidth();
        }
        if (mHeight < 0) {
            mHeight = getHeight();
        }
        if (mLocalBeginTs < 0) {
            mLocalBeginTs = System.currentTimeMillis();
        }
        float divideLineTX = mReadLineX * mWidth;

        boolean isLowStart = true;
        long duration = System.currentTimeMillis() - mLocalBeginTs;// 流逝了这么多的物理时间
        for (LyricsLineInfo lyricsLineInfo : mLyricsLineInfoList) {
            float left = divideLineTX + (lyricsLineInfo.getStartTime() - mTranslateTX - duration) * SPEED / 1000;
            float right = divideLineTX + (lyricsLineInfo.getEndTime() - mTranslateTX - duration) * SPEED / 1000;
            float top = isLowStart ? U.getDisplayUtils().dip2px(40) : U.getDisplayUtils().dip2px(20);
            float bottom = top + U.getDisplayUtils().dip2px(7);
            if (right < left) {
                MyLog.w(TAG, "right<left? error");
                continue;
            }
            if (right <= divideLineTX) {
                RectF rectF = new RectF();
                rectF.left = left;
                rectF.right = right;
                rectF.top = top;
                rectF.bottom = bottom;
                canvas.drawRoundRect(rectF, U.getDisplayUtils().dip2px(10), U.getDisplayUtils().dip2px(10), mLeftPaint);
            } else if (left < divideLineTX && right > divideLineTX) {
                RectF rectLeftF = new RectF();
                rectLeftF.left = left;
                rectLeftF.right = divideLineTX;
                rectLeftF.top = top;
                rectLeftF.bottom = bottom;
                canvas.drawRoundRect(rectLeftF, U.getDisplayUtils().dip2px(10), U.getDisplayUtils().dip2px(10), mLeftPaint);
                RectF rectRightF = new RectF();
                rectRightF.left = divideLineTX;
                rectRightF.right = right;
                rectRightF.top = top;
                rectRightF.bottom = bottom;
                canvas.drawRoundRect(rectRightF, U.getDisplayUtils().dip2px(10), U.getDisplayUtils().dip2px(10), mRightPaint);
                mRedCy = (top + bottom) / 2;
            } else if (left >= divideLineTX) {
                RectF rectF = new RectF();
                rectF.left = left;
                rectF.right = right;
                rectF.top = top;
                rectF.bottom = bottom;
                canvas.drawRoundRect(rectF, U.getDisplayUtils().dip2px(10), U.getDisplayUtils().dip2px(10), mRightPaint);
            }
            isLowStart = !isLowStart;
        }
        if (mRedCy > 0) {
            canvas.drawCircle(divideLineTX, mRedCy, U.getDisplayUtils().dip2px(9), mRedOutpaint);
            canvas.drawCircle(divideLineTX, mRedCy, U.getDisplayUtils().dip2px(6), mRedInnerpaint);
        }
        if (!mLyricsLineInfoList.isEmpty()) {
            LyricsLineInfo last = mLyricsLineInfoList.get(mLyricsLineInfoList.size() - 1);
            if (last.getEndTime() - mTranslateTX - duration > 0) {
                // 还能画，让时间继续流逝
                postInvalidateDelayed(30);
            }
        }

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

}
